/*
// Copyright 2017 The Draco Authors.
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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_GEOMETRIC_NORMAL_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_GEOMETRIC_NORMAL_DECODER_H_

#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_decoder.h"
#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_geometric_normal_predictor_area.h"
#include "draco/compression/bit_coders/rans_bit_decoder.h"
#include "draco/draco_features.h"

namespace draco {

// See MeshPredictionSchemeGeometricNormalEncoder for documentation.
template <typename DataTypeT, class TransformT, class MeshDataT>
class MeshPredictionSchemeGeometricNormalDecoder
    : public MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT> {
 public:
  using CorrType = typename MeshPredictionSchemeDecoder<DataTypeT, TransformT,
                                                        MeshDataT>::CorrType;
  MeshPredictionSchemeGeometricNormalDecoder(const PointAttribute *attribute,
                                             const TransformT &transform,
                                             const MeshDataT &mesh_data)
      : MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT>(
            attribute, transform, mesh_data),
        predictor_(mesh_data) {}

 private:
  MeshPredictionSchemeGeometricNormalDecoder() {}

 public:
  bool ComputeOriginalValues(const CorrType *in_corr, DataTypeT *out_data,
                             int size, int num_components,
                             const PointIndex *entry_to_point_id_map) override;

  bool DecodePredictionData(DecoderBuffer *buffer) override;

  PredictionSchemeMethod GetPredictionMethod() const override {
    return MESH_PREDICTION_GEOMETRIC_NORMAL;
  }

  bool IsInitialized() const override {
    if (!predictor_.IsInitialized()) {
      return false;
    }
    if (!this->mesh_data().IsInitialized()) {
      return false;
    }
    if (!octahedron_tool_box_.IsInitialized()) {
      return false;
    }
    return true;
  }

  int GetNumParentAttributes() const override { return 1; }

  GeometryAttribute::Type GetParentAttributeType(int i) const override {
    DRACO_DCHECK_EQ(i, 0);
    (void)i;
    return GeometryAttribute::POSITION;
  }

  bool SetParentAttribute(const PointAttribute *att) override {
    if (att->attribute_type() != GeometryAttribute::POSITION) {
      return false;  // Invalid attribute type.
    }
    if (att->num_components() != 3) {
      return false;  // Currently works only for 3 component positions.
    }
    predictor_.SetPositionAttribute(*att);
    return true;
  }
  void SetQuantizationBits(int q) {
    octahedron_tool_box_.SetQuantizationBits(q);
  }

 private:
  MeshPredictionSchemeGeometricNormalPredictorArea<DataTypeT, TransformT,
                                                   MeshDataT>
      predictor_;
  OctahedronToolBox octahedron_tool_box_;
  RAnsBitDecoder flip_normal_bit_decoder_;
};

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeGeometricNormalDecoder<
    DataTypeT, TransformT,
    MeshDataT>::ComputeOriginalValues(const CorrType *in_corr,
                                      DataTypeT *out_data, int,
                                      int num_components,
                                      const PointIndex *entry_to_point_id_map) {
  this->SetQuantizationBits(this->transform().quantization_bits());
  predictor_.SetEntryToPointIdMap(entry_to_point_id_map);
  DRACO_DCHECK(this->IsInitialized());

  // Expecting in_data in octahedral coordinates, i.e., portable attribute.
  DRACO_DCHECK_EQ(num_components, 2);

  const int corner_map_size =
      static_cast<int>(this->mesh_data().data_to_corner_map()->size());

  VectorD<int32_t, 3> pred_normal_3d;
  int32_t pred_normal_oct[2];

  for (int data_id = 0; data_id < corner_map_size; ++data_id) {
    const CornerIndex corner_id =
        this->mesh_data().data_to_corner_map()->at(data_id);
    predictor_.ComputePredictedValue(corner_id, pred_normal_3d.data());

    // Compute predicted octahedral coordinates.
    octahedron_tool_box_.CanonicalizeIntegerVector(pred_normal_3d.data());
    DRACO_DCHECK_EQ(pred_normal_3d.AbsSum(),
                    octahedron_tool_box_.center_value());
    if (flip_normal_bit_decoder_.DecodeNextBit()) {
      pred_normal_3d = -pred_normal_3d;
    }
    octahedron_tool_box_.IntegerVectorToQuantizedOctahedralCoords(
        pred_normal_3d.data(), pred_normal_oct, pred_normal_oct + 1);

    const int data_offset = data_id * 2;
    this->transform().ComputeOriginalValue(
        pred_normal_oct, in_corr + data_offset, out_data + data_offset);
  }
  flip_normal_bit_decoder_.EndDecoding();
  return true;
}

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeGeometricNormalDecoder<
    DataTypeT, TransformT, MeshDataT>::DecodePredictionData(DecoderBuffer
                                                                *buffer) {
  // Get data needed for transform
  if (!this->transform().DecodeTransformData(buffer)) {
    return false;
  }

#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (buffer->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
    uint8_t prediction_mode;
    if (!buffer->Decode(&prediction_mode)) {
      return false;
    }
    if (prediction_mode > TRIANGLE_AREA) {
      // Invalid prediction mode.
      return false;
    }

    if (!predictor_.SetNormalPredictionMode(
            NormalPredictionMode(prediction_mode))) {
      return false;
    }
  }
#endif

  // Init normal flips.
  if (!flip_normal_bit_decoder_.StartDecoding(buffer)) {
    return false;
  }

  return true;
}

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_GEOMETRIC_NORMAL_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.NormalPredictionMode;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MpsGeometricNormalDecoder<DataT, CorrT> extends MeshPredictionSchemeDecoder<DataT, CorrT> {

    private final MpsGeometricNormalPredictorArea<DataT> predictor;
    private final OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
    private final RAnsBitDecoder flipNormalBitDecoder = new RAnsBitDecoder();

    public MpsGeometricNormalDecoder(PointAttribute attribute,
                                     PredictionSchemeDecodingTransform<DataT, CorrT> transform,
                                     MeshPredictionSchemeData<?> meshData) {
        super(attribute, transform, meshData);
        DataNumberType<DataT, ?> dataType = this.getDataType();
        this.predictor = new MpsGeometricNormalPredictorArea<>(dataType, meshData);
    }

    @Override
    public Status computeOriginalValues(CppVector<CorrT> inCorr, CppVector<DataT> outData,
                                        int size, int numComponents, CppVector<PointIndex> entryToPointIdMap) {
        DataNumberType<DataT, ?> dataType = this.getDataType();
        this.setQuantizationBits(this.getTransform().getQuantizationBits());
        predictor.setEntryToPointIdMap(entryToPointIdMap);
        if(!this.isInitialized()) {
            return Status.dracoError("Not initialized");
        }

        // Expecting in_data in octahedral coordinates, i.e., portable attribute.
        if(numComponents != 2) {
            return Status.invalidParameter("Expecting 2 components");
        }

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();

        VectorD.I3 predNormal3D = new VectorD.I3();
        AtomicInteger s = new AtomicInteger();
        AtomicInteger t = new AtomicInteger();

        for(int dataId = 0; dataId < cornerMapSize; ++dataId) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(dataId);
            CppVector<DataT> prediction = CppVector.view(DataType.int32(), predNormal3D.getArray())
                    .cast(dataType::from, dataType::toInt);
            predictor.computePredictedValue(cornerId, prediction);

            // Compute predicted octahedral coordinates.
            octahedronToolBox.canonicalizeIntegerVector(DataType.int32(), predNormal3D.getArray());
            if(predNormal3D.absSum() != octahedronToolBox.getCenterValue()) {
                return Status.dracoError("Invalid sum");
            }
            if(flipNormalBitDecoder.decodeNextBit()) {
                predNormal3D = predNormal3D.negate();
            }
            octahedronToolBox.integerVectorToQuantizedOctahedralCoords(predNormal3D.getArray(), s, t);

            int dataOffset = dataId * 2;
            CppVector<DataT> predNormalOct = CppVector.view(DataType.int32(), new int[] { s.get(), t.get() })
                    .cast(dataType::from, dataType::toInt);
            this.getTransform().computeOriginalValue(predNormalOct,
                    inCorr.withOffset(dataOffset), outData.withOffset(dataOffset));
        }
        flipNormalBitDecoder.endDecoding();
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        // Get data needed for transform
        if(this.getTransform().decodeTransformData(buffer).isError(chain)) return chain.get();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            AtomicReference<UByte> predictionModeRef = new AtomicReference<>();
            if(buffer.decode(DataType.uint8(), predictionModeRef::set).isError(chain)) return chain.get();
            NormalPredictionMode predictionMode = NormalPredictionMode.valueOf(predictionModeRef.get().intValue());
            if(predictionMode == null) {
                return Status.ioError("Invalid prediction mode");
            }
            if(predictor.setNormalPredictionMode(predictionMode).isError(chain)) return chain.get();
        }

        // Init normal flips.
        return flipNormalBitDecoder.startDecoding(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_PREDICTION_GEOMETRIC_NORMAL;
    }

    @Override
    public boolean isInitialized() {
        return predictor.isInitialized() && this.getMeshData().isInitialized() && octahedronToolBox.isInitialized();
    }

    @Override
    public int getNumParentAttributes() {
        return 1;
    }

    @Override
    public GeometryAttribute.Type getParentAttributeType(int i) {
        if(i != 0) {
            throw new IllegalArgumentException("Invalid parent attribute index");
        }
        return GeometryAttribute.Type.POSITION;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        if(att.getAttributeType() != GeometryAttribute.Type.POSITION) {
            return Status.invalidParameter("Invalid attribute type");
        }
        if(!att.getNumComponents().equals(3)) {
            return Status.invalidParameter("Currently works only for 3 component positions");
        }
        predictor.setPositionAttribute(att);
        return Status.ok();
    }

    public void setQuantizationBits(int q) {
        octahedronToolBox.setQuantizationBits(q);
    }
}
