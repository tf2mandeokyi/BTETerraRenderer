package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

public class RAnsSymbolDecoder implements SymbolDecoder {

    private final CppVector<UInt> probabilityTable = CppVector.create(DataType.uint32());
    @Getter
    private UInt numSymbols = UInt.ZERO;
    private final RAnsDecoder ans;

    public RAnsSymbolDecoder(int uniqueSymbolsBitLength) {
        int ransPrecisionBits = Ans.computeRAnsPrecisionFromUniqueSymbolsBitLength(uniqueSymbolsBitLength);
        this.ans = new RAnsDecoder(ransPrecisionBits);
    }

    @Override
    public Status create(DecoderBuffer buffer) {
        StatusChain chain = Status.newChain();

        // Check that the DecoderBuffer version is set.
        if(buffer.getBitstreamVersion() == 0) {
            return new Status(Status.Code.DRACO_ERROR, "Buffer version not set");
        }
        // Decode the number of alphabet symbols.
        AtomicReference<UInt> numSymbolsRef = new AtomicReference<>();
        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint32(), numSymbolsRef::set).isError(chain)) return chain.get();
        } else {
            if(BitUtils.decodeVarint(DataType.uint32(), numSymbolsRef, buffer).isError(chain)) return chain.get();
        }
        this.numSymbols = numSymbolsRef.get();

        // Check that decoded number of symbols is not unreasonably high. Remaining
        // buffer size must be at least |num_symbols| / 64 bytes to contain the
        // probability table. The |prob_data| below is one byte but it can be
        // theoretically stored for each 64th symbol.
        if (numSymbols.div(64).gt(buffer.getRemainingSize())) {
            return new Status(Status.Code.IO_ERROR, "Decoded number of symbols is unreasonably high");
        }
        probabilityTable.resize(numSymbols.intValue());
        if(numSymbols.equals(0)) return Status.OK;

        // Decode the table.
        for (UInt i = UInt.ZERO; i.lt(numSymbols); i = i.add(1)) {
            AtomicReference<UByte> probDataRef = new AtomicReference<>();
            // Decode the first byte and extract the number of extra bytes we need to
            // get, or the offset to the next symbol with non-zero probability.
            if(buffer.decode(DataType.uint8(), probDataRef::set).isError(chain)) return chain.get();
            UByte probData = probDataRef.get();

            // Token is stored in the first two bits of the first byte. Values 0-2 are
            // used to indicate the number of extra bytes, and value 3 is a special
            // symbol used to denote run-length coding of zero probability entries.
            // See rans_symbol_encoder.h for more details.
            int token = probData.and(3).intValue();
            if(token == 3) {
                UInt offset = probData.shr(2).uIntValue();
                if(offset.add(i).ge(numSymbols)) {
                    return new Status(Status.Code.IO_ERROR, "Offset out of bounds");
                }
                // Set zero probability for all symbols in the specified range.
                for (UInt j = UInt.ZERO, until = offset.add(1); j.lt(until); j = j.add(1)) {
                    probabilityTable.set(i.add(j), UInt.ZERO);
                }
                i = i.add(offset);
            } else {
                UInt prob = probData.shr(2).uIntValue();
                for (int b = 0; b < token; ++b) {
                    AtomicReference<UByte> ebRef = new AtomicReference<>();
                    if(buffer.decode(DataType.uint8(), ebRef::set).isError(chain)) return chain.get();
                    UByte eb = ebRef.get();
                    // Shift 8 bits for each extra byte and subtract 2 for the two first
                    // bits.
                    prob = prob.or(eb.uIntValue().shl(8 * (b + 1) - 2));
                }
                probabilityTable.set(i, prob);
            }
        }
        return ans.ransBuildLookUpTable(probabilityTable, numSymbols);
    }

    @Override
    public Status startDecoding(DecoderBuffer buffer) {
        StatusChain chain = Status.newChain();

        // Decode the number of bytes encoded by the encoder.
        AtomicReference<ULong> bytesEncodedRef = new AtomicReference<>();
        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint64(), bytesEncodedRef::set).isError(chain)) return chain.get();
        } else {
            if(BitUtils.decodeVarint(DataType.uint64(), bytesEncodedRef, buffer).isError(chain)) return chain.get();
        }
        ULong bytesEncoded = bytesEncodedRef.get();

        if(bytesEncoded.gt(buffer.getRemainingSize())) {
            return new Status(Status.Code.IO_ERROR, "Bytes encoded exceeds buffer size");
        }
        DataBuffer dataHead = buffer.getDataHead();
        // Advance the buffer past the rANS data.
        buffer.advance(bytesEncoded.longValue());
        return ans.readInit(dataHead, bytesEncoded.longValue());
    }

    @Override
    public UInt decodeSymbol() {
        return ans.ransRead();
    }

    @Override
    public void endDecoding() {
        ans.readEnd();
    }

}
