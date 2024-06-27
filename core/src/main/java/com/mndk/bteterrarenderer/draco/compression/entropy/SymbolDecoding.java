package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.compression.config.SymbolCodingMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class SymbolDecoding {

    public Status decodeSymbols(UInt numValues, int numComponents,
                                DecoderBuffer srcBuffer, CppVector<UInt> outValues) {
        StatusChain chain = Status.newChain();

        if(numValues.equals(0)) return Status.OK;

        // Decode which scheme to use.
        AtomicReference<UByte> schemeRef = new AtomicReference<>();
        if(srcBuffer.decode(DataType.uint8(), schemeRef::set).isError(chain)) return chain.get();
        SymbolCodingMethod scheme = SymbolCodingMethod.fromValue(schemeRef.get());

        if(scheme == SymbolCodingMethod.SYMBOL_CODING_TAGGED) {
            return decodeTaggedSymbols(RAnsSymbolDecoder::new, numValues, numComponents, srcBuffer, outValues);
        } else if(scheme == SymbolCodingMethod.SYMBOL_CODING_RAW) {
            return decodeRawSymbols(RAnsSymbolDecoder::new, numValues, srcBuffer, outValues);
        }
        return new Status(Status.Code.IO_ERROR, "Invalid symbol coding method: " + schemeRef.get());
    }

    private Status decodeTaggedSymbols(Function<Integer, SymbolDecoder> decoderMaker, UInt numValues, int numComponents,
                                       DecoderBuffer srcBuffer, CppVector<UInt> outValues) {
        StatusChain chain = Status.newChain();

        SymbolDecoder tagDecoder = decoderMaker.apply(5);
        if(tagDecoder.create(srcBuffer).isError(chain)) return chain.get();

        if(tagDecoder.startDecoding(srcBuffer).isError(chain)) return chain.get();

        if(numValues.gt(0) && tagDecoder.getNumSymbols().equals(0)) {
            return new Status(Status.Code.DRACO_ERROR, "Wrong number of symbols.");
        }

        // srcBuffer now points behind the encoded tag data (to the place where the
        // values are encoded).
        srcBuffer.startBitDecoding(false, val -> {});
        int valueId = 0;
        for(UInt i = UInt.ZERO; i.lt(numValues); i = i.add(numComponents)) {
            // Decode the tag.
            UInt bitLength = tagDecoder.decodeSymbol();
            // Decode the actual value.
            for(int j = 0; j < numComponents; j++) {
                AtomicReference<UInt> val = new AtomicReference<>();
                if(srcBuffer.decodeLeastSignificantBits32(bitLength, val::set).isError(chain)) return chain.get();
                outValues.set(valueId++, val.get());
            }
        }
        tagDecoder.endDecoding();
        srcBuffer.endBitDecoding();
        return Status.OK;
    }

    private Status decodeRawSymbolsInternal(Supplier<SymbolDecoder> decoderMaker, UInt numValues,
                                             DecoderBuffer srcBuffer, CppVector<UInt> outValues) {
        StatusChain chain = Status.newChain();

        SymbolDecoder decoder = decoderMaker.get();
        if(decoder.create(srcBuffer).isError(chain)) return chain.get();

        if(numValues.gt(0) && decoder.getNumSymbols().equals(0)) {
            return new Status(Status.Code.DRACO_ERROR, "Wrong number of symbols.");
        }

        if(decoder.startDecoding(srcBuffer).isError(chain)) return chain.get();
        for(UInt i = UInt.ZERO; i.lt(numValues); i = i.add(1)) {
            // Decode a symbol into the value.
            UInt value = decoder.decodeSymbol();
            outValues.set(i, value);
        }
        decoder.endDecoding();
        return Status.OK;
    }

    private Status decodeRawSymbols(Function<Integer, SymbolDecoder> decoderMaker, UInt numValues,
                                    DecoderBuffer srcBuffer, CppVector<UInt> outValues) {
        StatusChain chain = Status.newChain();

        AtomicReference<UByte> maxBitLengthRef = new AtomicReference<>();
        if(srcBuffer.decode(DataType.uint8(), maxBitLengthRef::set).isError(chain)) return chain.get();
        int maxBitLength = maxBitLengthRef.get().intValue();

        if(maxBitLength < 1 || maxBitLength > 18) {
            return new Status(Status.Code.IO_ERROR, "Invalid max bit length: " + maxBitLength);
        }
        return decodeRawSymbolsInternal(() -> decoderMaker.apply(maxBitLength), numValues, srcBuffer, outValues);
    }

}
