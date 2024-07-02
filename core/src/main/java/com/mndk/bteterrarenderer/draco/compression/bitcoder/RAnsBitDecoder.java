/*
// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// File provides basic classes and functions for rANS coding.
#ifndef DRACO_COMPRESSION_BIT_CODERS_RANS_BIT_DECODER_H_
#define DRACO_COMPRESSION_BIT_CODERS_RANS_BIT_DECODER_H_

#include <vector>

#include "draco/compression/entropy/ans.h"
#include "draco/core/decoder_buffer.h"
#include "draco/draco_features.h"

namespace draco {

// Class for decoding a sequence of bits that were encoded with RAnsBitEncoder.
class RAnsBitDecoder {
 public:
  RAnsBitDecoder();
  ~RAnsBitDecoder();

  // Sets |source_buffer| as the buffer to decode bits from.
  // Returns false when the data is invalid.
  bool StartDecoding(DecoderBuffer *source_buffer);

  // Decode one bit. Returns true if the bit is a 1, otherwise false.
  bool DecodeNextBit();

  // Decode the next |nbits| and return the sequence in |value|. |nbits| must be
  // > 0 and <= 32.
  void DecodeLeastSignificantBits32(int nbits, uint32_t *value);

  void EndDecoding() {}

 private:
  void Clear();

  AnsDecoder ans_decoder_;
  uint8_t prob_zero_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_BIT_CODERS_RANS_BIT_DECODER_H_
 */

/*
// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
#include "draco/compression/bit_coders/rans_bit_decoder.h"

#include "draco/compression/config/compression_shared.h"
#include "draco/core/bit_utils.h"
#include "draco/core/varint_decoding.h"

namespace draco {

RAnsBitDecoder::RAnsBitDecoder() : prob_zero_(0) {}

RAnsBitDecoder::~RAnsBitDecoder() { Clear(); }

bool RAnsBitDecoder::StartDecoding(DecoderBuffer *source_buffer) {
  Clear();

  if (!source_buffer->Decode(&prob_zero_)) {
    return false;
  }

  uint32_t size_in_bytes;
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (source_buffer->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
    if (!source_buffer->Decode(&size_in_bytes)) {
      return false;
    }

  } else
#endif
  {
    if (!DecodeVarint(&size_in_bytes, source_buffer)) {
      return false;
    }
  }

  if (size_in_bytes > source_buffer->remaining_size()) {
    return false;
  }

  if (ans_read_init(&ans_decoder_,
                    reinterpret_cast<uint8_t *>(
                        const_cast<char *>(source_buffer->data_head())),
                    size_in_bytes) != 0) {
    return false;
  }
  source_buffer->Advance(size_in_bytes);
  return true;
}

bool RAnsBitDecoder::DecodeNextBit() {
  const uint8_t bit = rabs_read(&ans_decoder_, prob_zero_);
  return bit > 0;
}

void RAnsBitDecoder::DecodeLeastSignificantBits32(int nbits, uint32_t *value) {
  DRACO_DCHECK_EQ(true, nbits <= 32);
  DRACO_DCHECK_EQ(true, nbits > 0);

  uint32_t result = 0;
  while (nbits) {
    result = (result << 1) + DecodeNextBit();
    --nbits;
  }
  *value = result;
}

void RAnsBitDecoder::Clear() { ans_read_end(&ans_decoder_); }

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.bitcoder;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.Ans;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;
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
        if(sourceBuffer.decode(DataType.uint8(), val -> this.probZero = val).isError(chain)) return chain.get();

        AtomicReference<UInt> sizeInBytesRef;
        if(sourceBuffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            sizeInBytesRef = new AtomicReference<>();
            if(sourceBuffer.decode(DataType.uint32(), sizeInBytesRef::set).isError(chain)) return chain.get();
        } else {
            sizeInBytesRef = new AtomicReference<>();
            if(sourceBuffer.decodeVarint(DataType.uint32(), sizeInBytesRef).isError(chain)) return chain.get();
        }
        int sizeInBytes = sizeInBytesRef.get().intValue();

        if(sizeInBytes > sourceBuffer.getRemainingSize()) {
            return Status.ioError("Decoded number of symbols is unreasonably high");
        }

        DataBuffer data = sourceBuffer.getDataHead();
        if(ansDecoder.ansReadInit(data, sizeInBytes).isError(chain)) return chain.get();
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
