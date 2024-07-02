/*
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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_CONSTRAINED_MULTI_PARALLELOGRAM_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_CONSTRAINED_MULTI_PARALLELOGRAM_DECODER_H_

#include <algorithm>
#include <cmath>

#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_constrained_multi_parallelogram_shared.h"
#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_decoder.h"
#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_parallelogram_shared.h"
#include "draco/compression/bit_coders/rans_bit_decoder.h"
#include "draco/core/math_utils.h"
#include "draco/core/varint_decoding.h"
#include "draco/draco_features.h"

namespace draco {

// Decoder for predictions encoded with the constrained multi-parallelogram
// encoder. See the corresponding encoder for more details about the prediction
// method.
template <typename DataTypeT, class TransformT, class MeshDataT>
class MeshPredictionSchemeConstrainedMultiParallelogramDecoder
    : public MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT> {
 public:
  using CorrType =
      typename PredictionSchemeDecoder<DataTypeT, TransformT>::CorrType;
  using CornerTable = typename MeshDataT::CornerTable;

  explicit MeshPredictionSchemeConstrainedMultiParallelogramDecoder(
      const PointAttribute *attribute)
      : MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT>(
            attribute),
        selected_mode_(Mode::OPTIMAL_MULTI_PARALLELOGRAM) {}
  MeshPredictionSchemeConstrainedMultiParallelogramDecoder(
      const PointAttribute *attribute, const TransformT &transform,
      const MeshDataT &mesh_data)
      : MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT>(
            attribute, transform, mesh_data),
        selected_mode_(Mode::OPTIMAL_MULTI_PARALLELOGRAM) {}

  bool ComputeOriginalValues(const CorrType *in_corr, DataTypeT *out_data,
                             int size, int num_components,
                             const PointIndex *entry_to_point_id_map) override;

  bool DecodePredictionData(DecoderBuffer *buffer) override;

  PredictionSchemeMethod GetPredictionMethod() const override {
    return MESH_PREDICTION_CONSTRAINED_MULTI_PARALLELOGRAM;
  }

  bool IsInitialized() const override {
    return this->mesh_data().IsInitialized();
  }

 private:
  typedef constrained_multi_parallelogram::Mode Mode;
  static constexpr int kMaxNumParallelograms =
      constrained_multi_parallelogram::kMaxNumParallelograms;
  // Crease edges are used to store whether any given edge should be used for
  // parallelogram prediction or not. New values are added in the order in which
  // the edges are processed. For better compression, the flags are stored in
  // in separate contexts based on the number of available parallelograms at a
  // given vertex.
  std::vector<bool> is_crease_edge_[kMaxNumParallelograms];
  Mode selected_mode_;
};

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeConstrainedMultiParallelogramDecoder<
    DataTypeT, TransformT, MeshDataT>::
    ComputeOriginalValues(const CorrType *in_corr, DataTypeT *out_data,
                          int, int num_components,
                          const PointIndex *) {
  this->transform().Init(num_components);

  // Predicted values for all simple parallelograms encountered at any given
  // vertex.
  std::vector<DataTypeT> pred_vals[kMaxNumParallelograms];
  for (int i = 0; i < kMaxNumParallelograms; ++i) {
    pred_vals[i].resize(num_components, 0);
  }
  this->transform().ComputeOriginalValue(pred_vals[0].data(), in_corr,
                                         out_data);

  const CornerTable *const table = this->mesh_data().corner_table();
  const std::vector<int32_t> *const vertex_to_data_map =
      this->mesh_data().vertex_to_data_map();

  // Current position in the |is_crease_edge_| array for each context.
  std::vector<int> is_crease_edge_pos(kMaxNumParallelograms, 0);

  // Used to store predicted value for multi-parallelogram prediction.
  std::vector<DataTypeT> multi_pred_vals(num_components);

  const int corner_map_size =
      static_cast<int>(this->mesh_data().data_to_corner_map()->size());
  for (int p = 1; p < corner_map_size; ++p) {
    const CornerIndex start_corner_id =
        this->mesh_data().data_to_corner_map()->at(p);

    CornerIndex corner_id(start_corner_id);
    int num_parallelograms = 0;
    bool first_pass = true;
    while (corner_id != kInvalidCornerIndex) {
      if (ComputeParallelogramPrediction(
              p, corner_id, table, *vertex_to_data_map, out_data,
              num_components, &(pred_vals[num_parallelograms][0]))) {
        // Parallelogram prediction applied and stored in
        // |pred_vals[num_parallelograms]|
        ++num_parallelograms;
        // Stop processing when we reach the maximum number of allowed
        // parallelograms.
        if (num_parallelograms == kMaxNumParallelograms) {
          break;
        }
      }

      // Proceed to the next corner attached to the vertex. First swing left
      // and if we reach a boundary, swing right from the start corner.
      if (first_pass) {
        corner_id = table->SwingLeft(corner_id);
      } else {
        corner_id = table->SwingRight(corner_id);
      }
      if (corner_id == start_corner_id) {
        break;
      }
      if (corner_id == kInvalidCornerIndex && first_pass) {
        first_pass = false;
        corner_id = table->SwingRight(start_corner_id);
      }
    }

    // Check which of the available parallelograms are actually used and compute
    // the final predicted value.
    int num_used_parallelograms = 0;
    if (num_parallelograms > 0) {
      for (int i = 0; i < num_components; ++i) {
        multi_pred_vals[i] = 0;
      }
      // Check which parallelograms are actually used.
      for (int i = 0; i < num_parallelograms; ++i) {
        const int context = num_parallelograms - 1;
        const int pos = is_crease_edge_pos[context]++;
        if (is_crease_edge_[context].size() <= pos) {
          return false;
        }
        const bool is_crease = is_crease_edge_[context][pos];
        if (!is_crease) {
          ++num_used_parallelograms;
          for (int j = 0; j < num_components; ++j) {
            multi_pred_vals[j] =
                AddAsUnsigned(multi_pred_vals[j], pred_vals[i][j]);
          }
        }
      }
    }
    const int dst_offset = p * num_components;
    if (num_used_parallelograms == 0) {
      // No parallelogram was valid.
      // We use the last decoded point as a reference.
      const int src_offset = (p - 1) * num_components;
      this->transform().ComputeOriginalValue(
          out_data + src_offset, in_corr + dst_offset, out_data + dst_offset);
    } else {
      // Compute the correction from the predicted value.
      for (int c = 0; c < num_components; ++c) {
        multi_pred_vals[c] /= num_used_parallelograms;
      }
      this->transform().ComputeOriginalValue(
          multi_pred_vals.data(), in_corr + dst_offset, out_data + dst_offset);
    }
  }
  return true;
}

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeConstrainedMultiParallelogramDecoder<
    DataTypeT, TransformT, MeshDataT>::DecodePredictionData(DecoderBuffer
                                                                *buffer) {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (buffer->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
    // Decode prediction mode.
    uint8_t mode;
    if (!buffer->Decode(&mode)) {
      return false;
    }

    if (mode != Mode::OPTIMAL_MULTI_PARALLELOGRAM) {
      // Unsupported mode.
      return false;
    }
  }
#endif

  // Encode selected edges using separate rans bit coder for each context.
  for (int i = 0; i < kMaxNumParallelograms; ++i) {
    uint32_t num_flags;
    if (!DecodeVarint<uint32_t>(&num_flags, buffer)) {
      return false;
    }
    if (num_flags > this->mesh_data().corner_table()->num_corners()) {
      return false;
    }
    if (num_flags > 0) {
      is_crease_edge_[i].resize(num_flags);
      RAnsBitDecoder decoder;
      if (!decoder.StartDecoding(buffer)) {
        return false;
      }
      for (uint32_t j = 0; j < num_flags; ++j) {
        is_crease_edge_[i][j] = decoder.DecodeNextBit();
      }
      decoder.EndDecoding();
    }
  }
  return MeshPredictionSchemeDecoder<DataTypeT, TransformT,
                                     MeshDataT>::DecodePredictionData(buffer);
}

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_CONSTRAINED_MULTI_PARALLELOGRAM_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MpsConstrainedMultiParallelogramDecoder<DataT, CorrT> extends MeshPredictionSchemeDecoder<DataT, CorrT> {

    public static final UByte OPTIMAL_MULTI_PARALLELOGRAM = UByte.ZERO;
    public static final int MAX_NUM_PARALLELOGRAMS = 4;

    private final List<CppVector<Boolean>> isCreaseEdge = new ArrayList<>(); {
        for (int i = 0; i < MAX_NUM_PARALLELOGRAMS; ++i) {
            isCreaseEdge.add(CppVector.create(DataType.bool(), MAX_NUM_PARALLELOGRAMS));
        }
    }
    // private Mode selectedMode; // This is never used

    public MpsConstrainedMultiParallelogramDecoder(PointAttribute attribute,
                                                   PredictionSchemeDecodingTransform<DataT, CorrT> transform,
                                                   MeshPredictionSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeOriginalValues(CppVector<CorrT> inCorr, CppVector<DataT> outData,
                                        int size, int numComponents, CppVector<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);

        // Predicted values for all simple parallelograms encountered at any given vertex.
        DataNumberType<DataT, ?> dataType = this.getDataType();
        List<CppVector<DataT>> predVals = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_PARALLELOGRAMS; ++i) {
            predVals.add(CppVector.create(this.getDataType(), numComponents, dataType.from(0)));
        }
        this.getTransform().computeOriginalValue(predVals.get(0), inCorr, outData);

        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();

        // Current position in the isCreaseEdge array for each context.
        CppVector<Integer> isCreaseEdgePos = CppVector.create(DataType.int32(), MAX_NUM_PARALLELOGRAMS, 0);

        // Used to store predicted value for multi-parallelogram prediction.
        CppVector<DataT> multiPredVals = CppVector.create(dataType, numComponents);

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();
        for (int p = 1; p < cornerMapSize; ++p) {
            CornerIndex startCornerId = this.getMeshData().getDataToCornerMap().get(p);
            CornerIndex cornerId = startCornerId;
            int numParallelograms = 0;
            boolean firstPass = true;
            while (cornerId.isValid()) {
                Status status = MpsParallelogram.computeParallelogramPrediction(
                        p, cornerId, table, vertexToDataMap, dataType, outData, numComponents, predVals.get(numParallelograms));
                if (status.isError()) {
                    // Parallelogram prediction applied and stored in predVals[numParallelograms]
                    ++numParallelograms;
                    // Stop processing when we reach the maximum number of allowed parallelograms.
                    if (numParallelograms == MAX_NUM_PARALLELOGRAMS) {
                        break;
                    }
                }

                // Proceed to the next corner attached to the vertex. First swing left
                // and if we reach a boundary, swing right from the start corner.
                if (firstPass) {
                    cornerId = table.swingLeft(cornerId);
                } else {
                    cornerId = table.swingRight(cornerId);
                }
                if (cornerId.equals(startCornerId)) {
                    break;
                }
                if (cornerId.isInvalid() && firstPass) {
                    firstPass = false;
                    cornerId = table.swingRight(startCornerId);
                }
            }

            // Check which of the available parallelograms are actually used and compute the final predicted value.
            int numUsedParallelograms = 0;
            if (numParallelograms > 0) {
                for (int i = 0; i < numComponents; ++i) {
                    multiPredVals.set(i, dataType.from(0));
                }
                // Check which parallelograms are actually used.
                for (int i = 0; i < numParallelograms; ++i) {
                    int context = numParallelograms - 1;
                    int pos = isCreaseEdgePos.get(context);
                    isCreaseEdgePos.set(context, pos + 1);
                    if (isCreaseEdge.get(context).size() <= pos) {
                        return Status.ioError("Index out of bounds");
                    }
                    boolean isCrease = isCreaseEdge.get(context).get(pos);
                    if (!isCrease) {
                        ++numUsedParallelograms;
                        for (int j = 0; j < numComponents; ++j) {
                            multiPredVals.set(j, dataType.add(multiPredVals.get(j), predVals.get(i).get(j)));
                        }
                    }
                }
            }
            int dstOffset = p * numComponents;
            if (numUsedParallelograms == 0) {
                // No parallelogram was valid.
                // We use the last decoded point as a reference.
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeOriginalValue(
                        outData.withOffset(srcOffset), inCorr.withOffset(dstOffset), outData.withOffset(dstOffset));
            } else {
                // Compute the correction from the predicted value.
                for (int c = 0; c < numComponents; ++c) {
                    multiPredVals.set(c, dataType.div(multiPredVals.get(c), numUsedParallelograms));
                }
                this.getTransform().computeOriginalValue(
                        multiPredVals, inCorr.withOffset(dstOffset), outData.withOffset(dstOffset));
            }
        }
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            // Decode prediction mode.
            AtomicReference<UByte> modeRef = new AtomicReference<>();
            if(buffer.decode(DataType.uint8(), modeRef::set).isError(chain)) return chain.get();
            UByte mode = modeRef.get();
            if(!mode.equals(OPTIMAL_MULTI_PARALLELOGRAM)) {
                // Unsupported mode.
                return Status.ioError("Unsupported mode");
            }
        }

        // Encode selected edges using separate rans bit coder for each context.
        for (int i = 0; i < MAX_NUM_PARALLELOGRAMS; ++i) {
            AtomicReference<UInt> numFlagsRef = new AtomicReference<>();
            if(buffer.decodeVarint(DataType.uint32(), numFlagsRef).isError(chain)) return chain.get();
            int numFlags = numFlagsRef.get().intValue();
            if (numFlags > this.getMeshData().getCornerTable().getNumCorners()) {
                return Status.ioError("numFlags > cornerTable.getNumCorners()");
            }
            if (numFlags > 0) {
                isCreaseEdge.get(i).resize(numFlags);
                RAnsBitDecoder decoder = new RAnsBitDecoder();
                if (decoder.startDecoding(buffer).isError(chain)) return chain.get();
                for (int j = 0; j < numFlags; ++j) {
                    isCreaseEdge.get(i).set(j, decoder.decodeNextBit());
                }
                decoder.endDecoding();
            }
        }
        return super.decodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_PREDICTION_CONSTRAINED_MULTI_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }
}
