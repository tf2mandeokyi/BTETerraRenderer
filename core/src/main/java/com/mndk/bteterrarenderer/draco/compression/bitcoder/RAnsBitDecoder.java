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

package com.mndk.bteterrarenderer.draco.compression.bitcoder;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.Ans;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;

import java.util.concurrent.atomic.AtomicReference;

public class RAnsBitDecoder {

    private final Ans.Decoder ansDecoder = new Ans.Decoder();
    private UByte probZero = UByte.ZERO;

    /**
     * Sets {@code sourceBuffer} as the buffer to decode bits from.
     * Returns error when the data is invalid.
     */
    public Status startDecoding(DecoderBuffer sourceBuffer) {
        StatusChain chain = new StatusChain();

        this.clear();
        Pointer<UByte> probZeroRef = Pointer.newUByte();
        if(sourceBuffer.decode(probZeroRef).isError(chain)) return chain.get();
        this.probZero = probZeroRef.get();

        Pointer<UInt> sizeInBytesRef = Pointer.newUInt();
        if(sourceBuffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(sourceBuffer.decode(sizeInBytesRef).isError(chain)) return chain.get();
        } else {
            if(sourceBuffer.decodeVarint(sizeInBytesRef).isError(chain)) return chain.get();
        }
        int sizeInBytes = sizeInBytesRef.get().intValue();

        if(sizeInBytes > sourceBuffer.getRemainingSize()) {
            return Status.ioError("Decoded number of symbols is unreasonably high");
        }

        if(ansDecoder.ansReadInit(sourceBuffer.getDataHead(), sizeInBytes).isError(chain)) return chain.get();
        sourceBuffer.advance(sizeInBytes);
        return Status.ok();
    }

    /** Decode one bit. Returns true if the bit is a 1, otherwise false. */
    public boolean decodeNextBit() {
        return ansDecoder.rabsRead(probZero);
    }

    public void decodeLeastSignificantBits32(int nBits, AtomicReference<UInt> value) {
        if(nBits <= 0 || nBits > 32) {
            throw new IllegalArgumentException("number of bits(got " + nBits + ") must be > 0 and <= 32");
        }

        UInt result = UInt.ZERO;
        while(nBits > 0) {
            result = result.shl(1).add(ansDecoder.rabsRead(probZero) ? 1 : 0);
            nBits--;
        }
        value.set(result);
    }

    public void endDecoding() {}

    private void clear() {
        ansDecoder.ansReadEnd();
    }

}
