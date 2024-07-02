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
#ifndef DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_H_
#define DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_H_

#include "draco/compression/mesh/mesh_decoder.h"
#include "draco/compression/mesh/mesh_edgebreaker_decoder_impl_interface.h"
#include "draco/draco_features.h"

namespace draco {

// Class for decoding data encoded by MeshEdgebreakerEncoder.
class MeshEdgebreakerDecoder : public MeshDecoder {
 public:
  MeshEdgebreakerDecoder();

  const CornerTable *GetCornerTable() const override {
    return impl_->GetCornerTable();
  }

  const MeshAttributeCornerTable *GetAttributeCornerTable(
      int att_id) const override {
    return impl_->GetAttributeCornerTable(att_id);
  }

  const MeshAttributeIndicesEncodingData *GetAttributeEncodingData(
      int att_id) const override {
    return impl_->GetAttributeEncodingData(att_id);
  }

 protected:
  bool InitializeDecoder() override;
  bool CreateAttributesDecoder(int32_t att_decoder_id) override;
  bool DecodeConnectivity() override;
  bool OnAttributesDecoded() override;

  std::unique_ptr<MeshEdgebreakerDecoderImplInterface> impl_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_H_

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
#include "draco/compression/mesh/mesh_edgebreaker_decoder.h"

#include "draco/compression/mesh/mesh_edgebreaker_decoder_impl.h"
#include "draco/compression/mesh/mesh_edgebreaker_traversal_predictive_decoder.h"
#include "draco/compression/mesh/mesh_edgebreaker_traversal_valence_decoder.h"

namespace draco {

MeshEdgebreakerDecoder::MeshEdgebreakerDecoder() {}

bool MeshEdgebreakerDecoder::CreateAttributesDecoder(int32_t att_decoder_id) {
  return impl_->CreateAttributesDecoder(att_decoder_id);
}

bool MeshEdgebreakerDecoder::InitializeDecoder() {
  uint8_t traversal_decoder_type;
  if (!buffer()->Decode(&traversal_decoder_type)) {
    return false;
  }
  impl_ = nullptr;
  if (traversal_decoder_type == MESH_EDGEBREAKER_STANDARD_ENCODING) {
#ifdef DRACO_STANDARD_EDGEBREAKER_SUPPORTED
    impl_ = std::unique_ptr<MeshEdgebreakerDecoderImplInterface>(
        new MeshEdgebreakerDecoderImpl<MeshEdgebreakerTraversalDecoder>());
#endif
  } else if (traversal_decoder_type == MESH_EDGEBREAKER_PREDICTIVE_ENCODING) {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
#ifdef DRACO_PREDICTIVE_EDGEBREAKER_SUPPORTED
    impl_ = std::unique_ptr<MeshEdgebreakerDecoderImplInterface>(
        new MeshEdgebreakerDecoderImpl<
            MeshEdgebreakerTraversalPredictiveDecoder>());
#endif
#endif
  } else if (traversal_decoder_type == MESH_EDGEBREAKER_VALENCE_ENCODING) {
    impl_ = std::unique_ptr<MeshEdgebreakerDecoderImplInterface>(
        new MeshEdgebreakerDecoderImpl<
            MeshEdgebreakerTraversalValenceDecoder>());
  }
  if (!impl_) {
    return false;
  }
  if (!impl_->Init(this)) {
    return false;
  }
  return true;
}

bool MeshEdgebreakerDecoder::DecodeConnectivity() {
  return impl_->DecodeConnectivity();
}

bool MeshEdgebreakerDecoder::OnAttributesDecoded() {
  return impl_->OnAttributesDecoded();
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEdgebreakerConnectivityEncodingMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;

import java.util.concurrent.atomic.AtomicReference;

public class MeshEdgebreakerDecoder extends MeshDecoder {

    private MeshEdgebreakerDecoderImplInterface impl;

    @Override
    public CornerTable getCornerTable() {
        return impl.getCornerTable();
    }

    @Override
    public MeshAttributeCornerTable getAttributeCornerTable(int index) {
        return impl.getAttributeCornerTable(index);
    }

    @Override
    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int index) {
        return impl.getAttributeEncodingData(index);
    }

    @Override
    protected Status createAttributesDecoder(int attDecoderId) {
        return impl.createAttributesDecoder(attDecoderId);
    }

    @Override
    protected Status initializeDecoder() {
        StatusChain chain = new StatusChain();
        AtomicReference<UByte> traversalDecoderTypeRef = new AtomicReference<>();
        if(this.getBuffer().decode(DataType.uint8(), traversalDecoderTypeRef::set).isError(chain)) return chain.get();
        MeshEdgebreakerConnectivityEncodingMethod traversalDecoderType =
                MeshEdgebreakerConnectivityEncodingMethod.valueOf(traversalDecoderTypeRef.get());
        if(traversalDecoderType == null) {
            return Status.ioError("Invalid traversal decoder type: " + traversalDecoderTypeRef.get());
        }

        impl = null;
        switch(traversalDecoderType) {
            case MESH_EDGEBREAKER_STANDARD_ENCODING:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalDecoder());
                break;
            case MESH_EDGEBREAKER_PREDICTIVE_ENCODING:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalPredictiveDecoder());
                break;
            case MESH_EDGEBREAKER_VALENCE_ENCODING:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalValenceDecoder());
                break;
        }

        return impl.init(this);
    }

    @Override
    protected Status decodeConnectivity() {
        return impl.decodeConnectivity();
    }

    @Override
    protected Status onAttributesDecoded() {
        return impl.onAttributesDecoded();
    }
}
