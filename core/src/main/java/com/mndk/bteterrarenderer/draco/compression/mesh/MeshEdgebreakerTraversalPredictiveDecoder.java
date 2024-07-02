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
#ifndef DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_PREDICTIVE_DECODER_H_
#define DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_PREDICTIVE_DECODER_H_

#include "draco/compression/mesh/mesh_edgebreaker_traversal_decoder.h"
#include "draco/draco_features.h"

namespace draco {

// Decoder for traversal encoded with the
// MeshEdgebreakerTraversalPredictiveEncoder. The decoder maintains valences
// of the decoded portion of the traversed mesh and it uses them to predict
// symbols that are about to be decoded.
class MeshEdgebreakerTraversalPredictiveDecoder
    : public MeshEdgebreakerTraversalDecoder {
 public:
  MeshEdgebreakerTraversalPredictiveDecoder()
      : corner_table_(nullptr),
        num_vertices_(0),
        last_symbol_(-1),
        predicted_symbol_(-1) {}
  void Init(MeshEdgebreakerDecoderImplInterface *decoder) {
    MeshEdgebreakerTraversalDecoder::Init(decoder);
    corner_table_ = decoder->GetCornerTable();
  }
  void SetNumEncodedVertices(int num_vertices) { num_vertices_ = num_vertices; }

  bool Start(DecoderBuffer *out_buffer) {
    if (!MeshEdgebreakerTraversalDecoder::Start(out_buffer)) {
      return false;
    }
    int32_t num_split_symbols;
    if (!out_buffer->Decode(&num_split_symbols) || num_split_symbols < 0)
      return false;
    if (num_split_symbols >= num_vertices_) {
      return false;
    }
    // Set the valences of all initial vertices to 0.
    vertex_valences_.resize(num_vertices_, 0);
    if (!prediction_decoder_.StartDecoding(out_buffer)) {
      return false;
    }
    return true;
  }

  inline uint32_t DecodeSymbol() {
    // First check if we have a predicted symbol.
    if (predicted_symbol_ != -1) {
      // Double check that the predicted symbol was predicted correctly.
      if (prediction_decoder_.DecodeNextBit()) {
        last_symbol_ = predicted_symbol_;
        return predicted_symbol_;
      }
    }
    // We don't have a predicted symbol or the symbol was mis-predicted.
    // Decode it directly.
    last_symbol_ = MeshEdgebreakerTraversalDecoder::DecodeSymbol();
    return last_symbol_;
  }

  inline void NewActiveCornerReached(CornerIndex corner) {
    const CornerIndex next = corner_table_->Next(corner);
    const CornerIndex prev = corner_table_->Previous(corner);
    // Update valences.
    switch (last_symbol_) {
      case TOPOLOGY_C:
      case TOPOLOGY_S:
        vertex_valences_[corner_table_->Vertex(next).value()] += 1;
        vertex_valences_[corner_table_->Vertex(prev).value()] += 1;
        break;
      case TOPOLOGY_R:
        vertex_valences_[corner_table_->Vertex(corner).value()] += 1;
        vertex_valences_[corner_table_->Vertex(next).value()] += 1;
        vertex_valences_[corner_table_->Vertex(prev).value()] += 2;
        break;
      case TOPOLOGY_L:
        vertex_valences_[corner_table_->Vertex(corner).value()] += 1;
        vertex_valences_[corner_table_->Vertex(next).value()] += 2;
        vertex_valences_[corner_table_->Vertex(prev).value()] += 1;
        break;
      case TOPOLOGY_E:
        vertex_valences_[corner_table_->Vertex(corner).value()] += 2;
        vertex_valences_[corner_table_->Vertex(next).value()] += 2;
        vertex_valences_[corner_table_->Vertex(prev).value()] += 2;
        break;
      default:
        break;
    }
    // Compute the new predicted symbol.
    if (last_symbol_ == TOPOLOGY_C || last_symbol_ == TOPOLOGY_R) {
      const VertexIndex pivot =
          corner_table_->Vertex(corner_table_->Next(corner));
      if (vertex_valences_[pivot.value()] < 6) {
        predicted_symbol_ = TOPOLOGY_R;
      } else {
        predicted_symbol_ = TOPOLOGY_C;
      }
    } else {
      predicted_symbol_ = -1;
    }
  }

  inline void MergeVertices(VertexIndex dest, VertexIndex source) {
    // Update valences on the merged vertices.
    vertex_valences_[dest.value()] += vertex_valences_[source.value()];
  }

 private:
  const CornerTable *corner_table_;
  int num_vertices_;
  std::vector<int> vertex_valences_;
  BinaryDecoder prediction_decoder_;
  int last_symbol_;
  int predicted_symbol_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_PREDICTIVE_DECODER_H_
#endif

 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;

import java.util.concurrent.atomic.AtomicReference;

public class MeshEdgebreakerTraversalPredictiveDecoder extends MeshEdgebreakerTraversalDecoder {

    private CornerTable cornerTable = null;
    private int numVertices = 0;
    private final CppVector<Integer> vertexValences = CppVector.create(DataType.int32());
    private final RAnsBitDecoder predictionDecoder = new RAnsBitDecoder();
    private EdgebreakerTopology lastSymbol = EdgebreakerTopology.INVALID;
    private EdgebreakerTopology predictedSymbol = EdgebreakerTopology.INVALID;

    @Override
    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        super.init(decoder);
        cornerTable = decoder.getCornerTable();
    }

    @Override
    public void setNumEncodedVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    @Override
    public Status start(AtomicReference<DecoderBuffer> outBufferRef) {
        StatusChain chain = new StatusChain();
        if(super.start(outBufferRef).isError(chain)) return chain.get();
        DecoderBuffer outBuffer = outBufferRef.get();

        AtomicReference<Integer> numSplitSymbolsRef = new AtomicReference<>();
        if(outBuffer.decode(DataType.int32(), numSplitSymbolsRef::set).isError(chain)) return chain.get();
        int numSplitSymbols = numSplitSymbolsRef.get();
        if(numSplitSymbols < 0 || numSplitSymbols >= numVertices) {
            return Status.ioError("Invalid number of split symbols: " + numSplitSymbols + " (numVertices = " + numVertices + ")");
        }

        // Set the valences of all initial vertices to 0.
        vertexValences.resize(numVertices, 0);
        return predictionDecoder.startDecoding(outBuffer);
    }

    @Override
    public EdgebreakerTopology decodeSymbol() {
        // First check if we have a predicted symbol.
        if(predictedSymbol != EdgebreakerTopology.INVALID) {
            // Double check that the predicted symbol was predicted correctly.
            if(predictionDecoder.decodeNextBit()) {
                lastSymbol = predictedSymbol;
                return predictedSymbol;
            }
        }
        // We don't have a predicted symbol or the symbol was mis-predicted.
        // Decode it directly.
        return super.decodeSymbol();
    }

    @Override
    public void newActiveCornerReached(CornerIndex corner) {
        CornerIndex next = cornerTable.next(corner);
        CornerIndex prev = cornerTable.previous(corner);
        // Update valences.
        switch(lastSymbol) {
            case C:
            case S:
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 1);
                break;
            case R:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 2);
                break;
            case L:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 1);
                break;
            case E:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 2);
                break;
            default:
                break;
        }
        // Compute the new predicted symbol.
        if(lastSymbol == EdgebreakerTopology.C || lastSymbol == EdgebreakerTopology.R) {
            int pivot = cornerTable.getVertex(cornerTable.next(corner)).getValue();
            if(vertexValences.get(pivot) < 6) {
                predictedSymbol = EdgebreakerTopology.R;
            } else {
                predictedSymbol = EdgebreakerTopology.C;
            }
        } else {
            predictedSymbol = EdgebreakerTopology.INVALID;
        }
    }

    @Override
    public void mergeVertices(VertexIndex dest, VertexIndex source) {
        // Update valences on the merged vertices.
        vertexValences.set(dest.getValue(), val -> val + vertexValences.get(source.getValue()));
    }
}
