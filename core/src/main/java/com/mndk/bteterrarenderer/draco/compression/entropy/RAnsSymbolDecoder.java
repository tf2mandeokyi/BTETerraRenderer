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

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.Getter;

public class RAnsSymbolDecoder implements SymbolDecoder {

    private final CppVector<UInt> probabilityTable = new CppVector<>(DataType.uint32());
    @Getter
    private UInt numSymbols = UInt.ZERO;
    private final RAnsDecoder ans;

    public RAnsSymbolDecoder(int uniqueSymbolsBitLength) {
        int ransPrecisionBits = Ans.computeRAnsPrecisionFromUniqueSymbolsBitLength(uniqueSymbolsBitLength);
        this.ans = new RAnsDecoder(ransPrecisionBits);
    }

    @Override
    public Status create(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        // Check that the DecoderBuffer version is set.
        if (buffer.getBitstreamVersion() == 0) {
            return Status.dracoError("Buffer version not set");
        }
        // Decode the number of alphabet symbols.
        Pointer<UInt> numSymbolsRef = Pointer.newUInt();
        if (buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if (buffer.decode(numSymbolsRef).isError(chain)) return chain.get();
        } else {
            if (buffer.decodeVarint(numSymbolsRef).isError(chain)) return chain.get();
        }
        this.numSymbols = numSymbolsRef.get();

        // Check that decoded number of symbols is not unreasonably high. Remaining
        // buffer size must be at least |num_symbols| / 64 bytes to contain the
        // probability table. The |prob_data| below is one byte but it can be
        // theoretically stored for each 64th symbol.
        if (numSymbols.div(64).gt(buffer.getRemainingSize())) {
            return Status.ioError("Decoded number of symbols is unreasonably high");
        }
        probabilityTable.resize(numSymbols.intValue());
        if (numSymbols.equals(0)) return Status.ok();

        // Decode the table.
        for (UInt i = UInt.ZERO; i.lt(numSymbols); i = i.add(1)) {
            Pointer<UByte> probDataRef = Pointer.newUByte();
            // Decode the first byte and extract the number of extra bytes we need to
            // get, or the offset to the next symbol with non-zero probability.
            if (buffer.decode(probDataRef).isError(chain)) return chain.get();
            UByte probData = probDataRef.get();

            // Token is stored in the first two bits of the first byte. Values 0-2 are
            // used to indicate the number of extra bytes, and value 3 is a special
            // symbol used to denote run-length coding of zero probability entries.
            // See rans_symbol_encoder.h for more details.
            int token = probData.and(3).intValue();
            if (token == 3) {
                UInt offset = probData.shr(2).uIntValue();
                if (offset.add(i).ge(numSymbols)) {
                    return Status.ioError("Offset out of bounds");
                }
                // Set zero probability for all symbols in the specified range.
                for (UInt j = UInt.ZERO, until = offset.add(1); j.lt(until); j = j.add(1)) {
                    probabilityTable.set(i.add(j), UInt.ZERO);
                }
                i = i.add(offset);
            } else {
                UInt prob = probData.shr(2).uIntValue();
                for (int b = 0; b < token; ++b) {
                    Pointer<UByte> ebRef = Pointer.newUByte();
                    if (buffer.decode(ebRef).isError(chain)) return chain.get();
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
        StatusChain chain = new StatusChain();

        // Decode the number of bytes encoded by the encoder.
        Pointer<ULong> bytesEncodedRef = Pointer.newULong();
        if (buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if (buffer.decode(bytesEncodedRef).isError(chain)) return chain.get();
        } else {
            if (buffer.decodeVarint(bytesEncodedRef).isError(chain)) return chain.get();
        }
        ULong bytesEncoded = bytesEncodedRef.get();

        if (bytesEncoded.gt(buffer.getRemainingSize())) {
            return Status.ioError("Bytes encoded exceeds buffer size");
        }
        RawPointer dataHead = buffer.getDataHead();
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
