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
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_TEX_COORDS_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_TEX_COORDS_DECODER_H_

#include <math.h>

#include "draco/compression/attributes/prediction_schemes/mesh_prediction_scheme_decoder.h"
#include "draco/compression/bit_coders/rans_bit_decoder.h"
#include "draco/core/varint_decoding.h"
#include "draco/core/vector_d.h"
#include "draco/draco_features.h"
#include "draco/mesh/corner_table.h"

namespace draco {

// Decoder for predictions of UV coordinates encoded by our specialized texture
// coordinate predictor. See the corresponding encoder for more details. Note
// that this predictor is not portable and should not be used anymore. See
// MeshPredictionSchemeTexCoordsPortableEncoder/Decoder for a portable version
// of this prediction scheme.
template <typename DataTypeT, class TransformT, class MeshDataT>
class MeshPredictionSchemeTexCoordsDecoder
    : public MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT> {
 public:
  using CorrType = typename MeshPredictionSchemeDecoder<DataTypeT, TransformT,
                                                        MeshDataT>::CorrType;
  MeshPredictionSchemeTexCoordsDecoder(const PointAttribute *attribute,
                                       const TransformT &transform,
                                       const MeshDataT &mesh_data, int version)
      : MeshPredictionSchemeDecoder<DataTypeT, TransformT, MeshDataT>(
            attribute, transform, mesh_data),
        pos_attribute_(nullptr),
        entry_to_point_id_map_(nullptr),
        num_components_(0),
        version_(version) {}

  bool ComputeOriginalValues(const CorrType *in_corr, DataTypeT *out_data,
                             int size, int num_components,
                             const PointIndex *entry_to_point_id_map) override;

  bool DecodePredictionData(DecoderBuffer *buffer) override;

  PredictionSchemeMethod GetPredictionMethod() const override {
    return MESH_PREDICTION_TEX_COORDS_DEPRECATED;
  }

  bool IsInitialized() const override {
    if (pos_attribute_ == nullptr) {
      return false;
    }
    if (!this->mesh_data().IsInitialized()) {
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
    if (att == nullptr) {
      return false;
    }
    if (att->attribute_type() != GeometryAttribute::POSITION) {
      return false;  // Invalid attribute type.
    }
    if (att->num_components() != 3) {
      return false;  // Currently works only for 3 component positions.
    }
    pos_attribute_ = att;
    return true;
  }

 protected:
  Vector3f GetPositionForEntryId(int entry_id) const {
    const PointIndex point_id = entry_to_point_id_map_[entry_id];
    Vector3f pos;
    pos_attribute_->ConvertValue(pos_attribute_->mapped_index(point_id),
                                 &pos[0]);
    return pos;
  }

  Vector2f GetTexCoordForEntryId(int entry_id, const DataTypeT *data) const {
    const int data_offset = entry_id * num_components_;
    return Vector2f(static_cast<float>(data[data_offset]),
                    static_cast<float>(data[data_offset + 1]));
  }

  bool ComputePredictedValue(CornerIndex corner_id, const DataTypeT *data,
                             int data_id);

 private:
  const PointAttribute *pos_attribute_;
  const PointIndex *entry_to_point_id_map_;
  std::unique_ptr<DataTypeT[]> predicted_value_;
  int num_components_;
  // Encoded / decoded array of UV flips.
  std::vector<bool> orientations_;
  int version_;
};

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeTexCoordsDecoder<DataTypeT, TransformT, MeshDataT>::
    ComputeOriginalValues(const CorrType *in_corr, DataTypeT *out_data,
                          int, int num_components,
                          const PointIndex *entry_to_point_id_map) {
        if (num_components != 2) {
        // Corrupt/malformed input. Two output components are req'd.
        return false;
        }
num_components_ = num_components;
entry_to_point_id_map_ = entry_to_point_id_map;
predicted_value_ =
std::unique_ptr<DataTypeT[]>(new DataTypeT[num_components]);
        this->transform().Init(num_components);

  const int corner_map_size =
        static_cast<int>(this->mesh_data().data_to_corner_map()->size());
        for (int p = 0; p < corner_map_size; ++p) {
        const CornerIndex corner_id = this->mesh_data().data_to_corner_map()->at(p);
    if (!ComputePredictedValue(corner_id, out_data, p)) {
        return false;
        }

        const int dst_offset = p * num_components;
    this->transform().ComputeOriginalValue(
        predicted_value_.get(), in_corr + dst_offset, out_data + dst_offset);
        }
        return true;
        }

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeTexCoordsDecoder<DataTypeT, TransformT, MeshDataT>::
DecodePredictionData(DecoderBuffer *buffer) {
    // Decode the delta coded orientations.
    uint32_t num_orientations = 0;
    if (buffer->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
        if (!buffer->Decode(&num_orientations)) {
            return false;
        }
    } else {
        if (!DecodeVarint(&num_orientations, buffer)) {
            return false;
        }
    }
    if (num_orientations == 0) {
        return false;
    }
    if (num_orientations > this->mesh_data().corner_table()->num_corners()) {
        // We can't have more orientations than the maximum number of decoded
        // values.
        return false;
    }
    orientations_.resize(num_orientations);
    bool last_orientation = true;
    RAnsBitDecoder decoder;
    if (!decoder.StartDecoding(buffer)) {
        return false;
    }
    for (uint32_t i = 0; i < num_orientations; ++i) {
        if (!decoder.DecodeNextBit()) {
            last_orientation = !last_orientation;
        }
        orientations_[i] = last_orientation;
    }
    decoder.EndDecoding();
    return MeshPredictionSchemeDecoder<DataTypeT, TransformT,
            MeshDataT>::DecodePredictionData(buffer);
}

template <typename DataTypeT, class TransformT, class MeshDataT>
bool MeshPredictionSchemeTexCoordsDecoder<DataTypeT, TransformT, MeshDataT>::
ComputePredictedValue(CornerIndex corner_id, const DataTypeT *data,
                      int data_id) {
    // Compute the predicted UV coordinate from the positions on all corners
    // of the processed triangle. For the best prediction, the UV coordinates
    // on the next/previous corners need to be already encoded/decoded.
  const CornerIndex next_corner_id =
            this->mesh_data().corner_table()->Next(corner_id);
  const CornerIndex prev_corner_id =
            this->mesh_data().corner_table()->Previous(corner_id);
    // Get the encoded data ids from the next and previous corners.
    // The data id is the encoding order of the UV coordinates.
    int next_data_id, prev_data_id;

    int next_vert_id, prev_vert_id;
    next_vert_id =
            this->mesh_data().corner_table()->Vertex(next_corner_id).value();
    prev_vert_id =
            this->mesh_data().corner_table()->Vertex(prev_corner_id).value();

    next_data_id = this->mesh_data().vertex_to_data_map()->at(next_vert_id);
    prev_data_id = this->mesh_data().vertex_to_data_map()->at(prev_vert_id);

    if (prev_data_id < data_id && next_data_id < data_id) {
        // Both other corners have available UV coordinates for prediction.
    const Vector2f n_uv = GetTexCoordForEntryId(next_data_id, data);
    const Vector2f p_uv = GetTexCoordForEntryId(prev_data_id, data);
        if (p_uv == n_uv) {
            // We cannot do a reliable prediction on degenerated UV triangles.
            // Technically floats > INT_MAX are undefined, but compilers will
            // convert those values to INT_MIN. We are being explicit here for asan.
            for (const int i : {0, 1}) {
                if (std::isnan(p_uv[i]) || static_cast<double>(p_uv[i]) > INT_MAX ||
                        static_cast<double>(p_uv[i]) < INT_MIN) {
                    predicted_value_[i] = INT_MIN;
                } else {
                    predicted_value_[i] = static_cast<int>(p_uv[i]);
                }
            }
            return true;
        }

        // Get positions at all corners.
    const Vector3f tip_pos = GetPositionForEntryId(data_id);
    const Vector3f next_pos = GetPositionForEntryId(next_data_id);
    const Vector3f prev_pos = GetPositionForEntryId(prev_data_id);
        // Use the positions of the above triangle to predict the texture coordinate
        // on the tip corner C.
        // Convert the triangle into a new coordinate system defined by orthogonal
        // bases vectors S, T, where S is vector prev_pos - next_pos and T is an
        // perpendicular vector to S in the same plane as vector the
        // tip_pos - next_pos.
        // The transformed triangle in the new coordinate system is then going to
        // be represented as:
        //
        //        1 ^
        //          |
        //          |
        //          |   C
        //          |  /  \
        //          | /      \
        //          |/          \
        //          N--------------P
        //          0              1
        //
        // Where next_pos point (N) is at position (0, 0), prev_pos point (P) is
        // at (1, 0). Our goal is to compute the position of the tip_pos point (C)
        // in this new coordinate space (s, t).
        //
    const Vector3f pn = prev_pos - next_pos;
    const Vector3f cn = tip_pos - next_pos;
    const float pn_norm2_squared = pn.SquaredNorm();
        // Coordinate s of the tip corner C is simply the dot product of the
        // normalized vectors |pn| and |cn| (normalized by the length of |pn|).
        // Since both of these vectors are normalized, we don't need to perform the
        // normalization explicitly and instead we can just use the squared norm
        // of |pn| as a denominator of the resulting dot product of non normalized
        // vectors.
        float s, t;
        // |pn_norm2_squared| can be exactly 0 when the next_pos and prev_pos are
        // the same positions (e.g. because they were quantized to the same
        // location).
        if (version_ < DRACO_BITSTREAM_VERSION(1, 2) || pn_norm2_squared > 0) {
            s = pn.Dot(cn) / pn_norm2_squared;
            // To get the coordinate t, we can use formula:
            //      t = |C-N - (P-N) * s| / |P-N|
            // Do not use std::sqrt to avoid changes in the bitstream.
            t = sqrt((cn - pn * s).SquaredNorm() / pn_norm2_squared);
        } else {
            s = 0;
            t = 0;
        }

        // Now we need to transform the point (s, t) to the texture coordinate space
        // UV. We know the UV coordinates on points N and P (N_UV and P_UV). Lets
        // denote P_UV - N_UV = PN_UV. PN_UV is then 2 dimensional vector that can
        // be used to define transformation from the normalized coordinate system
        // to the texture coordinate system using a 3x3 affine matrix M:
        //
        //  M = | PN_UV[0]  -PN_UV[1]  N_UV[0] |
        //      | PN_UV[1]   PN_UV[0]  N_UV[1] |
        //      | 0          0         1       |
        //
        // The predicted point C_UV in the texture space is then equal to
        // C_UV = M * (s, t, 1). Because the triangle in UV space may be flipped
        // around the PN_UV axis, we also need to consider point C_UV' = M * (s, -t)
        // as the prediction.
    const Vector2f pn_uv = p_uv - n_uv;
    const float pnus = pn_uv[0] * s + n_uv[0];
    const float pnut = pn_uv[0] * t;
    const float pnvs = pn_uv[1] * s + n_uv[1];
    const float pnvt = pn_uv[1] * t;
        Vector2f predicted_uv;
        if (orientations_.empty()) {
            return false;
        }

        // When decoding the data, we already know which orientation to use.
    const bool orientation = orientations_.back();
        orientations_.pop_back();
        if (orientation) {
            predicted_uv = Vector2f(pnus - pnvt, pnvs + pnut);
        } else {
            predicted_uv = Vector2f(pnus + pnvt, pnvs - pnut);
        }
        if (std::is_integral<DataTypeT>::value) {
            // Round the predicted value for integer types.
            // Technically floats > INT_MAX are undefined, but compilers will
            // convert those values to INT_MIN. We are being explicit here for asan.
      const double u = floor(predicted_uv[0] + 0.5);
            if (std::isnan(u) || u > INT_MAX || u < INT_MIN) {
                predicted_value_[0] = INT_MIN;
            } else {
                predicted_value_[0] = static_cast<int>(u);
            }
      const double v = floor(predicted_uv[1] + 0.5);
            if (std::isnan(v) || v > INT_MAX || v < INT_MIN) {
                predicted_value_[1] = INT_MIN;
            } else {
                predicted_value_[1] = static_cast<int>(v);
            }
        } else {
            predicted_value_[0] = static_cast<int>(predicted_uv[0]);
            predicted_value_[1] = static_cast<int>(predicted_uv[1]);
        }

        return true;
    }
    // Else we don't have available textures on both corners. For such case we
    // can't use positions for predicting the uv value and we resort to delta
    // coding.
    int data_offset = 0;
    if (prev_data_id < data_id) {
        // Use the value on the previous corner as the prediction.
        data_offset = prev_data_id * num_components_;
    }
    if (next_data_id < data_id) {
        // Use the value on the next corner as the prediction.
        data_offset = next_data_id * num_components_;
    } else {
        // None of the other corners have a valid value. Use the last encoded value
        // as the prediction if possible.
        if (data_id > 0) {
            data_offset = (data_id - 1) * num_components_;
        } else {
            // We are encoding the first value. Predict 0.
            for (int i = 0; i < num_components_; ++i) {
                predicted_value_[i] = 0;
            }
            return true;
        }
    }
    for (int i = 0; i < num_components_; ++i) {
        predicted_value_[i] = data[data_offset + i];
    }
    return true;
}

}  // namespace draco

        #endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_MESH_PREDICTION_SCHEME_TEX_COORDS_DECODER_H_
#endif

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

import java.util.concurrent.atomic.AtomicReference;

public class MpsTexCoordsDecoder<DataT, CorrT> extends MeshPredictionSchemeDecoder<DataT, CorrT> {

    private PointAttribute posAttribute = null;
    private CppVector<PointIndex> entryToPointIdMap = null;
    private CppVector<DataT> predictedValue;
    private int numComponents = 0;
    /** Encoded / decoded array of UV flips. */
    private final CppVector<Boolean> orientations = CppVector.create(DataType.bool());
    private final int version;

    public MpsTexCoordsDecoder(PointAttribute attribute,
                               PredictionSchemeDecodingTransform<DataT, CorrT> transform,
                               MeshPredictionSchemeData<?> meshData, UShort bitstreamVersion) {
        super(attribute, transform, meshData);
        this.version = bitstreamVersion.intValue();
    }

    @Override
    public Status computeOriginalValues(CppVector<CorrT> inCorr, CppVector<DataT> outData, int size, int numComponents, CppVector<PointIndex> entryToPointIdMap) {
        StatusChain chain = new StatusChain();

        if(numComponents != 2) {
            return Status.invalidParameter("Two output components are required");
        }
        this.numComponents = numComponents;
        this.entryToPointIdMap = entryToPointIdMap;
        predictedValue = CppVector.create(this.getDataType());
        this.getTransform().init(numComponents);

        for(int p = 0; p < this.getMeshData().getDataToCornerMap().size(); ++p) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            if(this.computePredictedValue(cornerId, outData, p).isError(chain)) return chain.get();
            int dstOffset = p * numComponents;
            this.getTransform().computeOriginalValue(
                    predictedValue, inCorr.withOffset(dstOffset), outData.withOffset(dstOffset));
        }
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        // Decode the delta coded orientations.
        AtomicReference<UInt> numOrientationsRef = new AtomicReference<>(UInt.ZERO);
        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(buffer.decode(DataType.uint32(), numOrientationsRef::set).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(DataType.uint32(), numOrientationsRef).isError(chain)) return chain.get();
        }
        int numOrientations = numOrientationsRef.get().intValue();
        if(numOrientations == 0) {
            return Status.ioError("Number of orientations is 0");
        }
        if(numOrientations > this.getMeshData().getCornerTable().getNumCorners()) {
            // We can't have more orientations than the maximum number of decoded
            // values.
            return Status.ioError("Number of orientations is greater than the number of corners");
        }
        orientations.resize(numOrientations);
        boolean lastOrientation = true;
        RAnsBitDecoder decoder = new RAnsBitDecoder();
        if(decoder.startDecoding(buffer).isError(chain)) return chain.get();
        for(int i = 0; i < numOrientations; ++i) {
            boolean orientation = decoder.decodeNextBit();
            if(orientation) {
                lastOrientation = !lastOrientation;
            }
            orientations.set(i, lastOrientation);
        }
        decoder.endDecoding();
        return super.decodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_PREDICTION_TEX_COORDS_DEPRECATED;
    }

    @Override
    public boolean isInitialized() {
        return posAttribute != null && this.getMeshData().isInitialized();
    }

    @Override
    public int getNumParentAttributes() {
        return 1;
    }

    @Override
    public GeometryAttribute.Type getParentAttributeType(int i) {
        if(i != 0) throw new IllegalArgumentException();
        return GeometryAttribute.Type.POSITION;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        if(att == null) {
            return Status.invalidParameter("Attribute is null");
        }
        if(att.getAttributeType() != GeometryAttribute.Type.POSITION) {
            return Status.invalidParameter("Invalid attribute type");
        }
        if(!att.getNumComponents().equals(3)) {
            return Status.invalidParameter("Currently works only for 3 component positions");
        }
        posAttribute = att;
        return Status.ok();
    }

    protected VectorD.F3 getPositionForEntryId(int entryId) {
        PointIndex pointId = entryToPointIdMap.get(entryId);
        VectorD.F3 pos = new VectorD.F3();
        posAttribute.convertValue(posAttribute.getMappedIndex(pointId), pos);
        return pos;
    }

    protected VectorD.F2 getTexCoordForEntryId(int entryId, CppVector<DataT> data) {
        int dataOffset = entryId * numComponents;
        DataNumberType<DataT, ?> dataType = this.getDataType();
        return new VectorD.F2(dataType.toFloat(data.get(dataOffset)), dataType.toFloat(data.get(dataOffset + 1)));
    }

    protected Status computePredictedValue(CornerIndex cornerId, CppVector<DataT> data, int dataId) {
        DataNumberType<DataT, ?> dataType = this.getDataType();
        // Compute the predicted UV coordinate from the positions on all corners
        // of the processed triangle.
        CornerIndex nextCornerId = this.getMeshData().getCornerTable().next(cornerId);
        CornerIndex prevCornerId = this.getMeshData().getCornerTable().previous(cornerId);
        // Get the encoded data ids from the next and previous corners.
        int nextVertId = this.getMeshData().getCornerTable().getVertex(nextCornerId).getValue();
        int prevVertId = this.getMeshData().getCornerTable().getVertex(prevCornerId).getValue();
        int nextDataId = this.getMeshData().getVertexToDataMap().get(nextVertId);
        int prevDataId = this.getMeshData().getVertexToDataMap().get(prevVertId);
        if (prevDataId < dataId && nextDataId < dataId) {
            // Both other corners have available UV coordinates for prediction.
            VectorD.F2 nUV = getTexCoordForEntryId(nextDataId, data);
            VectorD.F2 pUV = getTexCoordForEntryId(prevDataId, data);
            if (pUV.equals(nUV)) {
                // We cannot do a reliable prediction on degenerated UV triangles.
                for (int i = 0; i <= 1; i++) {
                    double pUVid = pUV.get(i);
                    if (Float.isNaN(pUV.get(i)) || pUVid > Integer.MAX_VALUE || pUVid < Integer.MIN_VALUE) {
                        predictedValue.set(i, dataType.from(Integer.MIN_VALUE));
                    } else {
                        predictedValue.set(i, dataType.from(pUV.get(i)));
                    }
                }
                return Status.ok();
            }
            // Get positions at all corners.
            VectorD.F3 tipPos = getPositionForEntryId(dataId);
            VectorD.F3 nextPos = getPositionForEntryId(nextDataId);
            VectorD.F3 prevPos = getPositionForEntryId(prevDataId);
            // Use the positions of the above triangle to predict the texture coordinate
            // on the tip corner C.
            VectorD.F3 pn = prevPos.subtract(nextPos);
            VectorD.F3 cn = tipPos.subtract(nextPos);
            float pnNorm2Squared = pn.squaredNorm();
            float s, t;
            if (version < DracoVersions.getBitstreamVersion(1, 2) || pnNorm2Squared > 0) {
                s = pn.dot(cn) / pnNorm2Squared;
                t = (float) Math.sqrt((cn.subtract(pn.multiply(s)).squaredNorm() / pnNorm2Squared));
            } else {
                s = 0;
                t = 0;
            }

            // Now we need to transform the point (s, t) to the texture coordinate space UV.
            VectorD.F2 pnUV = pUV.subtract(nUV);
            float pnus = pnUV.get(0) * s + nUV.get(0);
            float pnut = pnUV.get(0) * t;
            float pnvs = pnUV.get(1) * s + nUV.get(1);
            float pnvt = pnUV.get(1) * t;
            VectorD.F2 predictedUV;
            if (orientations.isEmpty()) {
                return Status.ioError("No orientations");
            }

            // When decoding the data, we already know which orientation to use.
            boolean orientation = orientations.popBack();
            if (orientation) {
                predictedUV = new VectorD.F2(pnus - pnvt, pnvs + pnut);
            } else {
                predictedUV = new VectorD.F2(pnus + pnvt, pnvs - pnut);
            }
            if(dataType.isIntegral()) {
                // Round the predicted value for integer types.
                double u = Math.floor(predictedUV.get(0) + 0.5);
                if (Double.isNaN(u) || u > Integer.MAX_VALUE || u < Integer.MIN_VALUE) {
                    predictedValue.set(0, dataType.from(Integer.MIN_VALUE));
                } else {
                    predictedValue.set(0, dataType.from((int) u));
                }
                double v = Math.floor(predictedUV.get(1) + 0.5);
                if (Double.isNaN(v) || v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                    predictedValue.set(1, dataType.from(Integer.MIN_VALUE));
                } else {
                    predictedValue.set(1, dataType.from((int) v));
                }
            } else {
                predictedValue.set(0, dataType.from(predictedUV.get(0)));
                predictedValue.set(1, dataType.from(predictedUV.get(1)));
            }
            return Status.ok();
        }
        // Else we don't have available textures on both corners.
        int dataOffset = 0;
        if (prevDataId < dataId) {
            // Use the value on the previous corner as the prediction.
            dataOffset = prevDataId * numComponents;
        }
        if (nextDataId < dataId) {
            // Use the value on the next corner as the prediction.
            dataOffset = nextDataId * numComponents;
        } else {
            // None of the other corners have a valid value. Use the last encoded value
            // as the prediction if possible.
            if (dataId > 0) {
                dataOffset = (dataId - 1) * numComponents;
            } else {
                // We are encoding the first value. Predict 0.
                for (int i = 0; i < numComponents; ++i) {
                    predictedValue.set(i, dataType.from(0));
                }
                return Status.ok();
            }
        }
        for (int i = 0; i < numComponents; ++i) {
            predictedValue.set(i, data.get(dataOffset + i));
        }
        return Status.ok();
    }
}
