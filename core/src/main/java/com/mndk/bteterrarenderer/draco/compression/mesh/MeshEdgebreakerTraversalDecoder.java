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
#ifndef DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_DECODER_H_
#define DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_DECODER_H_

#include "draco/compression/bit_coders/rans_bit_decoder.h"
#include "draco/compression/mesh/mesh_edgebreaker_decoder.h"
#include "draco/compression/mesh/mesh_edgebreaker_decoder_impl_interface.h"
#include "draco/compression/mesh/mesh_edgebreaker_shared.h"
#include "draco/draco_features.h"

namespace draco {

typedef RAnsBitDecoder BinaryDecoder;

// Default implementation of the edgebreaker traversal decoder that reads the
// traversal data directly from a buffer.
class MeshEdgebreakerTraversalDecoder {
 public:
  MeshEdgebreakerTraversalDecoder()
      : attribute_connectivity_decoders_(nullptr),
        num_attribute_data_(0),
        decoder_impl_(nullptr) {}
  void Init(MeshEdgebreakerDecoderImplInterface *decoder) {
    decoder_impl_ = decoder;
    buffer_.Init(decoder->GetDecoder()->buffer()->data_head(),
                 decoder->GetDecoder()->buffer()->remaining_size(),
                 decoder->GetDecoder()->buffer()->bitstream_version());
  }

  // Returns the Draco bitstream version.
  uint16_t BitstreamVersion() const {
    return decoder_impl_->GetDecoder()->bitstream_version();
  }

  // Used to tell the decoder what is the number of expected decoded vertices.
  // Ignored by default.
  void SetNumEncodedVertices(int) {}

// Set the number of non-position attribute data for which we need to decode
// the connectivity.
void SetNumAttributeData(int num_data) { num_attribute_data_ = num_data; }

// Called before the traversal decoding is started.
// Returns a buffer decoder that points to data that was encoded after the
// traversal.
bool Start(DecoderBuffer *out_buffer) {
    // Decode symbols from the main buffer decoder and face configurations from
    // the start_face_buffer decoder.
    if (!DecodeTraversalSymbols()) {
        return false;
    }

    if (!DecodeStartFaces()) {
        return false;
    }

    if (!DecodeAttributeSeams()) {
        return false;
    }
    *out_buffer = buffer_;
    return true;
}

// Returns the configuration of a new initial face.
inline bool DecodeStartFaceConfiguration() {
    uint32_t face_configuration;
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (buffer_.bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
        start_face_buffer_.DecodeLeastSignificantBits32(1, &face_configuration);

    } else
#endif
    {
        face_configuration = start_face_decoder_.DecodeNextBit();
    }
    return face_configuration;
}

// Returns the next edgebreaker symbol that was reached during the traversal.
inline uint32_t DecodeSymbol() {
    uint32_t symbol;
    symbol_buffer_.DecodeLeastSignificantBits32(1, &symbol);
    if (symbol == TOPOLOGY_C) {
        return symbol;
    }
    // Else decode two additional bits.
    uint32_t symbol_suffix;
    symbol_buffer_.DecodeLeastSignificantBits32(2, &symbol_suffix);
    symbol |= (symbol_suffix << 1);
    return symbol;
}

// Called whenever a new active corner is set in the decoder.
inline void NewActiveCornerReached(CornerIndex) {}

// Called whenever |source| vertex is about to be merged into the |dest|
// vertex.
inline void MergeVertices(VertexIndex, VertexIndex) {}

// Returns true if there is an attribute seam for the next processed pair
// of visited faces.
// |attribute| is used to mark the id of the non-position attribute (in range
// of <0, num_attributes - 1>).
inline bool DecodeAttributeSeam(int attribute) {
    return attribute_connectivity_decoders_[attribute].DecodeNextBit();
}

// Called when the traversal is finished.
void Done() {
    if (symbol_buffer_.bit_decoder_active()) {
        symbol_buffer_.EndBitDecoding();
    }
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (buffer_.bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
        start_face_buffer_.EndBitDecoding();

    } else
#endif
    {
        start_face_decoder_.EndDecoding();
    }
}

protected:
DecoderBuffer *buffer() { return &buffer_; }

bool DecodeTraversalSymbols() {
    uint64_t traversal_size;
    symbol_buffer_ = buffer_;
    if (!symbol_buffer_.StartBitDecoding(true, &traversal_size)) {
        return false;
    }
    buffer_ = symbol_buffer_;
    if (traversal_size > static_cast<uint64_t>(buffer_.remaining_size())) {
        return false;
    }
    buffer_.Advance(traversal_size);
    return true;
}

bool DecodeStartFaces() {
    // Create a decoder that is set to the end of the encoded traversal data.
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (buffer_.bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
        start_face_buffer_ = buffer_;
        uint64_t traversal_size;
        if (!start_face_buffer_.StartBitDecoding(true, &traversal_size)) {
            return false;
        }
        buffer_ = start_face_buffer_;
        if (traversal_size > static_cast<uint64_t>(buffer_.remaining_size())) {
            return false;
        }
        buffer_.Advance(traversal_size);
        return true;
    }
#endif
    return start_face_decoder_.StartDecoding(&buffer_);
}

bool DecodeAttributeSeams() {
    // Prepare attribute decoding.
    if (num_attribute_data_ > 0) {
        attribute_connectivity_decoders_ = std::unique_ptr<BinaryDecoder[]>(
                new BinaryDecoder[num_attribute_data_]);
        for (int i = 0; i < num_attribute_data_; ++i) {
            if (!attribute_connectivity_decoders_[i].StartDecoding(&buffer_)) {
                return false;
            }
        }
    }
    return true;
}

private:
// Buffer that contains the encoded data.
DecoderBuffer buffer_;
DecoderBuffer symbol_buffer_;
BinaryDecoder start_face_decoder_;
DecoderBuffer start_face_buffer_;
std::unique_ptr<BinaryDecoder[]> attribute_connectivity_decoders_;
int num_attribute_data_;
  const MeshEdgebreakerDecoderImplInterface *decoder_impl_;
};

        }  // namespace draco

        #endif  // DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicReference;

public class MeshEdgebreakerTraversalDecoder {

    @Setter(AccessLevel.PROTECTED)
    private DecoderBuffer buffer = new DecoderBuffer();
    private DecoderBuffer symbolBuffer = new DecoderBuffer();
    private final RAnsBitDecoder startFaceDecoder = new RAnsBitDecoder();
    private DecoderBuffer startFaceBuffer = new DecoderBuffer();
    private RAnsBitDecoder[] attributeConnectivityDecoders = null;
    @Setter
    private int numAttributeData = 0;
    private MeshEdgebreakerDecoderImplInterface decoderImpl = null;

    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        this.decoderImpl = decoder;
        DecoderBuffer buffer = decoder.getDecoder().getBuffer();
        this.buffer.init(buffer.getDataHead(), buffer.getRemainingSize(), buffer.getBitstreamVersion());
    }

    public int getBitstreamVersion() {
        return decoderImpl.getDecoder().getBitstreamVersion();
    }

    public Status start(AtomicReference<DecoderBuffer> outBufferRef) {
        StatusChain chain = new StatusChain();

        if(this.decodeTraversalSymbols().isError(chain)) return chain.get();
        if(this.decodeStartFaces().isError(chain)) return chain.get();
        if(this.decodeAttributeSeams().isError(chain)) return chain.get();

        outBufferRef.set(this.buffer);
        return Status.ok();
    }

    public boolean decodeStartFaceConfiguration() {
        StatusChain chain = new StatusChain();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            AtomicReference<UInt> faceConfigurationRef = new AtomicReference<>();
            Status status = startFaceBuffer.decodeLeastSignificantBits32(1, faceConfigurationRef::set);
            if(status.isError(chain)) throw status.getRuntimeException();
            return faceConfigurationRef.get().intValue() != 0;
        } else {
            return startFaceDecoder.decodeNextBit();
        }
    }

    public EdgebreakerTopology decodeSymbol() {
        AtomicReference<UInt> symbolRef = new AtomicReference<>();
        symbolBuffer.decodeLeastSignificantBits32(1, symbolRef::set);
        UInt symbol = symbolRef.get();
        if(EdgebreakerTopology.fromBitPattern(symbol) == EdgebreakerTopology.C) {
            return EdgebreakerTopology.C;
        }
        // Else decode two additional bits.
        AtomicReference<UInt> symbolSuffixRef = new AtomicReference<>();
        symbolBuffer.decodeLeastSignificantBits32(2, symbolSuffixRef::set);
        UInt symbolSuffix = symbolSuffixRef.get();
        return EdgebreakerTopology.fromBitPattern(symbol.or(symbolSuffix.shl(1)));
    }

    public void setNumEncodedVertices(int numVertices) {}
    public void newActiveCornerReached(CornerIndex corner) {}
    public void mergeVertices(VertexIndex dest, VertexIndex source) {}

    public boolean decodeAttributeSeam(int attribute) {
        return attributeConnectivityDecoders[attribute].decodeNextBit();
    }

    public void done() {
        if(symbolBuffer.isBitDecoderActive()) {
            symbolBuffer.endBitDecoding();
        }
        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            startFaceBuffer.endBitDecoding();
        } else {
            startFaceDecoder.endDecoding();
        }
    }

    protected Status decodeTraversalSymbols() {
        StatusChain chain = new StatusChain();

        AtomicReference<ULong> traversalSizeRef = new AtomicReference<>();
        this.symbolBuffer = this.buffer;
        if(symbolBuffer.startBitDecoding(true, traversalSizeRef::set).isError(chain)) return chain.get();
        ULong traversalSize = traversalSizeRef.get();
        this.buffer = this.symbolBuffer;
        if(traversalSize.gt(this.buffer.getRemainingSize())) {
            return Status.ioError("Traversal size is larger than remaining buffer size.");
        }
        this.buffer.advance(traversalSize.longValue());
        return Status.ok();
    }

    public Status decodeStartFaces() {
        StatusChain chain = new StatusChain();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            this.startFaceBuffer = this.buffer;
            AtomicReference<ULong> traversalSizeRef = new AtomicReference<>();
            if(startFaceBuffer.startBitDecoding(true, traversalSizeRef::set).isError(chain)) return chain.get();
            ULong traversalSize = traversalSizeRef.get();
            this.buffer = this.startFaceBuffer;
            if(traversalSize.gt(this.buffer.getRemainingSize())) {
                return Status.ioError("Traversal size is larger than remaining buffer size.");
            }
            this.buffer.advance(traversalSize.longValue());
            return Status.ok();
        }
        return this.startFaceDecoder.startDecoding(this.buffer);
    }

    public Status decodeAttributeSeams() {
        StatusChain chain = new StatusChain();

        if(numAttributeData > 0) {
            attributeConnectivityDecoders = new RAnsBitDecoder[numAttributeData];
            for(int i = 0; i < numAttributeData; ++i) {
                if(attributeConnectivityDecoders[i].startDecoding(buffer).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }
}
