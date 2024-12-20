/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.SymbolCodingMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class SymbolDecoding {

    public Status decode(UInt numValues, int numComponents, DecoderBuffer srcBuffer, Pointer<UInt> outValues) {
        StatusChain chain = new StatusChain();

        if (numValues.equals(0)) return Status.ok();

        // Decode which scheme to use.
        Pointer<UByte> schemeRef = Pointer.newUByte((byte) 0);
        if (srcBuffer.decode(schemeRef).isError(chain)) return chain.get();
        SymbolCodingMethod scheme = SymbolCodingMethod.valueOf(schemeRef.get());

        if (scheme == SymbolCodingMethod.SYMBOL_CODING_TAGGED) {
            return decodeTagged(RAnsSymbolDecoder::new, numValues, numComponents, srcBuffer, outValues);
        } else if (scheme == SymbolCodingMethod.SYMBOL_CODING_RAW) {
            return decodeRaw(RAnsSymbolDecoder::new, numValues, srcBuffer, outValues);
        }
        return Status.ioError("Invalid symbol coding method: " + schemeRef.get());
    }

    private Status decodeTagged(Function<Integer, SymbolDecoder> decoderMaker, UInt numValues, int numComponents,
                                DecoderBuffer srcBuffer, Pointer<UInt> outValues) {
        StatusChain chain = new StatusChain();

        SymbolDecoder tagDecoder = decoderMaker.apply(5);
        if (tagDecoder.create(srcBuffer).isError(chain)) return chain.get();

        if (tagDecoder.startDecoding(srcBuffer).isError(chain)) return chain.get();

        if (numValues.gt(0) && tagDecoder.getNumSymbols().equals(0)) {
            return Status.dracoError("Wrong number of symbols.");
        }

        // srcBuffer now points behind the encoded tag data (to the place where the
        // values are encoded).
        srcBuffer.startBitDecoding(false, Pointer.newULong());
        int valueId = 0;
        for (UInt i = UInt.ZERO; i.lt(numValues); i = i.add(numComponents)) {
            // Decode the tag.
            UInt bitLength = tagDecoder.decodeSymbol();
            // Decode the actual value.
            for (int j = 0; j < numComponents; j++) {
                Pointer<UInt> val = Pointer.newUInt();
                if (srcBuffer.decodeLeastSignificantBits32(bitLength, val).isError(chain)) return chain.get();
                outValues.set(valueId++, val.get());
            }
        }
        tagDecoder.endDecoding();
        srcBuffer.endBitDecoding();
        return Status.ok();
    }

    private Status decodeRawInternal(Supplier<SymbolDecoder> decoderMaker, UInt numValues,
                                     DecoderBuffer srcBuffer, Pointer<UInt> outValues) {
        StatusChain chain = new StatusChain();

        SymbolDecoder decoder = decoderMaker.get();
        if (decoder.create(srcBuffer).isError(chain)) return chain.get();

        if (numValues.gt(0) && decoder.getNumSymbols().equals(0)) {
            return Status.dracoError("Wrong number of symbols.");
        }

        if (decoder.startDecoding(srcBuffer).isError(chain)) return chain.get();
        for (int i = 0, until = numValues.intValue(); i < until; i++) {
            // Decode a symbol into the value.
            UInt value = decoder.decodeSymbol();
            outValues.set(i, value);
        }
        decoder.endDecoding();
        return Status.ok();
    }

    private Status decodeRaw(Function<Integer, SymbolDecoder> decoderMaker, UInt numValues,
                             DecoderBuffer srcBuffer, Pointer<UInt> outValues) {
        StatusChain chain = new StatusChain();

        Pointer<UByte> maxBitLengthRef = Pointer.newUByte();
        if (srcBuffer.decode(maxBitLengthRef).isError(chain)) return chain.get();
        int maxBitLength = maxBitLengthRef.get().intValue();

        if (maxBitLength < 1 || maxBitLength > 18) {
            return Status.ioError("Invalid max bit length: " + maxBitLength);
        }
        return decodeRawInternal(() -> decoderMaker.apply(maxBitLength), numValues, srcBuffer, outValues);
    }

}
