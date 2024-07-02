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
#ifndef DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_VALENCE_DECODER_H_
#define DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_VALENCE_DECODER_H_

#include "draco/compression/entropy/symbol_decoding.h"
#include "draco/compression/mesh/mesh_edgebreaker_traversal_decoder.h"
#include "draco/core/varint_decoding.h"
#include "draco/draco_features.h"

namespace draco {

// Decoder for traversal encoded with MeshEdgebreakerTraversalValenceEncoder.
// The decoder maintains valences of the decoded portion of the traversed mesh
// and it uses them to select entropy context used for decoding of the actual
// symbols.
class MeshEdgebreakerTraversalValenceDecoder
    : public MeshEdgebreakerTraversalDecoder {
 public:
  MeshEdgebreakerTraversalValenceDecoder()
      : corner_table_(nullptr),
        num_vertices_(0),
        last_symbol_(-1),
        active_context_(-1),
        min_valence_(2),
        max_valence_(7) {}
  void Init(MeshEdgebreakerDecoderImplInterface *decoder) {
    MeshEdgebreakerTraversalDecoder::Init(decoder);
    corner_table_ = decoder->GetCornerTable();
  }
  void SetNumEncodedVertices(int num_vertices) { num_vertices_ = num_vertices; }

  bool Start(DecoderBuffer *out_buffer) {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (BitstreamVersion() < DRACO_BITSTREAM_VERSION(2, 2)) {
      if (!MeshEdgebreakerTraversalDecoder::DecodeTraversalSymbols()) {
        return false;
      }
    }
#endif
    if (!MeshEdgebreakerTraversalDecoder::DecodeStartFaces()) {
      return false;
    }
    if (!MeshEdgebreakerTraversalDecoder::DecodeAttributeSeams()) {
      return false;
    }
    *out_buffer = *buffer();

#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (BitstreamVersion() < DRACO_BITSTREAM_VERSION(2, 2)) {
      uint32_t num_split_symbols;
      if (BitstreamVersion() < DRACO_BITSTREAM_VERSION(2, 0)) {
        if (!out_buffer->Decode(&num_split_symbols)) {
          return false;
        }
      } else {
        if (!DecodeVarint(&num_split_symbols, out_buffer)) {
          return false;
        }
      }
      if (num_split_symbols >= static_cast<uint32_t>(num_vertices_)) {
        return false;
      }

      int8_t mode;
      if (!out_buffer->Decode(&mode)) {
        return false;
      }
      if (mode == EDGEBREAKER_VALENCE_MODE_2_7) {
        min_valence_ = 2;
        max_valence_ = 7;
      } else {
        // Unsupported mode.
        return false;
      }

    } else
#endif
    {
      min_valence_ = 2;
      max_valence_ = 7;
    }

    if (num_vertices_ < 0) {
      return false;
    }
    // Set the valences of all initial vertices to 0.
    vertex_valences_.resize(num_vertices_, 0);

    const int num_unique_valences = max_valence_ - min_valence_ + 1;

    // Decode all symbols for all contexts.
    context_symbols_.resize(num_unique_valences);
    context_counters_.resize(context_symbols_.size());
    for (int i = 0; i < context_symbols_.size(); ++i) {
      uint32_t num_symbols;
      if (!DecodeVarint<uint32_t>(&num_symbols, out_buffer)) {
        return false;
      }
      if (num_symbols > static_cast<uint32_t>(corner_table_->num_faces())) {
        return false;
      }
      if (num_symbols > 0) {
        context_symbols_[i].resize(num_symbols);
        DecodeSymbols(num_symbols, 1, out_buffer, context_symbols_[i].data());
        // All symbols are going to be processed from the back.
        context_counters_[i] = num_symbols;
      }
    }
    return true;
  }

  inline uint32_t DecodeSymbol() {
    // First check if we have a valid context.
    if (active_context_ != -1) {
      const int context_counter = --context_counters_[active_context_];
      if (context_counter < 0) {
        return TOPOLOGY_INVALID;
      }
      const uint32_t symbol_id =
          context_symbols_[active_context_][context_counter];
      if (symbol_id > 4) {
        return TOPOLOGY_INVALID;
      }
      last_symbol_ = edge_breaker_symbol_to_topology_id[symbol_id];
    } else {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
      if (BitstreamVersion() < DRACO_BITSTREAM_VERSION(2, 2)) {
        // We don't have a predicted symbol or the symbol was mis-predicted.
        // Decode it directly.
        last_symbol_ = MeshEdgebreakerTraversalDecoder::DecodeSymbol();

      } else
#endif
      {
        // The first symbol must be E.
        last_symbol_ = TOPOLOGY_E;
      }
    }
    return last_symbol_;
  }

  inline void NewActiveCornerReached(CornerIndex corner) {
    const CornerIndex next = corner_table_->Next(corner);
    const CornerIndex prev = corner_table_->Previous(corner);
    // Update valences.
    switch (last_symbol_) {
      case TOPOLOGY_C:
      case TOPOLOGY_S:
        vertex_valences_[corner_table_->Vertex(next)] += 1;
        vertex_valences_[corner_table_->Vertex(prev)] += 1;
        break;
      case TOPOLOGY_R:
        vertex_valences_[corner_table_->Vertex(corner)] += 1;
        vertex_valences_[corner_table_->Vertex(next)] += 1;
        vertex_valences_[corner_table_->Vertex(prev)] += 2;
        break;
      case TOPOLOGY_L:
        vertex_valences_[corner_table_->Vertex(corner)] += 1;
        vertex_valences_[corner_table_->Vertex(next)] += 2;
        vertex_valences_[corner_table_->Vertex(prev)] += 1;
        break;
      case TOPOLOGY_E:
        vertex_valences_[corner_table_->Vertex(corner)] += 2;
        vertex_valences_[corner_table_->Vertex(next)] += 2;
        vertex_valences_[corner_table_->Vertex(prev)] += 2;
        break;
      default:
        break;
    }
    // Compute the new context that is going to be used to decode the next
    // symbol.
    const int active_valence = vertex_valences_[corner_table_->Vertex(next)];
    int clamped_valence;
    if (active_valence < min_valence_) {
      clamped_valence = min_valence_;
    } else if (active_valence > max_valence_) {
      clamped_valence = max_valence_;
    } else {
      clamped_valence = active_valence;
    }

    active_context_ = (clamped_valence - min_valence_);
  }

  inline void MergeVertices(VertexIndex dest, VertexIndex source) {
    // Update valences on the merged vertices.
    vertex_valences_[dest] += vertex_valences_[source];
  }

 private:
  const CornerTable *corner_table_;
  int num_vertices_;
  IndexTypeVector<VertexIndex, int> vertex_valences_;
  int last_symbol_;
  int active_context_;

  int min_valence_;
  int max_valence_;
  std::vector<std::vector<uint32_t>> context_symbols_;
  // Points to the active symbol in each context.
  std::vector<int> context_counters_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_TRAVERSAL_VALENCE_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.vector.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;

import java.util.concurrent.atomic.AtomicReference;

public class MeshEdgebreakerTraversalValenceDecoder extends MeshEdgebreakerTraversalDecoder {

    private CornerTable cornerTable = null;
    private int numVertices = 0;
    private final IndexTypeVector<VertexIndex, Integer> vertexValences =
            IndexTypeVector.create(VertexIndex::of, DataType.int32());
    private EdgebreakerTopology lastSymbol = EdgebreakerTopology.INVALID;
    private int activeContext = -1;

    private int minValence = 2;
    private int maxValence = 7;
    private final CppVector<CppVector<UInt>> contextSymbols = CppVector.create();
    private final CppVector<Integer> contextCounters = CppVector.create(DataType.int32());

    @Override
    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        super.init(decoder);
        this.cornerTable = decoder.getCornerTable();
    }

    @Override
    public void setNumEncodedVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    @Override
    public Status start(AtomicReference<DecoderBuffer> outBufferRef) {
        StatusChain chain = new StatusChain();

        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(super.decodeTraversalSymbols().isError(chain)) return chain.get();
        }
        if(super.decodeStartFaces().isError(chain)) return chain.get();
        if(super.decodeAttributeSeams().isError(chain)) return chain.get();
        DecoderBuffer outBuffer = outBufferRef.get();

        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            AtomicReference<UInt> numSplitSymbols = new AtomicReference<>();
            if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(outBuffer.decode(DataType.uint32(), numSplitSymbols::set).isError(chain)) return chain.get();
            } else {
                if(outBuffer.decodeVarint(DataType.uint32(), numSplitSymbols).isError(chain)) return chain.get();
            }
            UInt numSplitSymbolsValue = numSplitSymbols.get();
            if(numSplitSymbolsValue.ge(numVertices)) {
                return Status.dracoError("Invalid number of split symbols");
            }

            AtomicReference<Byte> modeRef = new AtomicReference<>();
            if(outBuffer.decode(DataType.int8(), modeRef::set).isError(chain)) return chain.get();
            byte mode = modeRef.get();
            if(mode == Edgebreaker.EDGEBREAKER_VALENCE_MODE_2_7) {
                minValence = 2;
                maxValence = 7;
            } else {
                return Status.ioError("Unsupported mode: " + mode);
            }
        } else {
            minValence = 2;
            maxValence = 7;
        }

        if(numVertices < 0) {
            return Status.dracoError("Invalid number of vertices");
        }
        // Set the valences of all initial vertices to 0.
        vertexValences.resize(numVertices, 0);

        int numUniqueValences = maxValence - minValence + 1;

        // Decode all symbols for all contexts.
        contextSymbols.resize(numUniqueValences, () -> CppVector.create(DataType.uint32()));
        contextCounters.resize(numUniqueValences);
        for(int i = 0; i < numUniqueValences; ++i) {
            AtomicReference<UInt> numSymbols = new AtomicReference<>();
            if(outBuffer.decodeVarint(DataType.uint32(), numSymbols).isError(chain)) return chain.get();
            UInt numSymbolsValue = numSymbols.get();
            if(numSymbolsValue.gt(cornerTable.getNumFaces())) {
                return Status.dracoError("Invalid number of symbols");
            }
            if(numSymbolsValue.gt(0)) {
                contextSymbols.get(i).resize(numSymbolsValue.intValue());
                SymbolDecoding.decode(numSymbolsValue, 1, outBuffer, contextSymbols.get(i));
                contextCounters.set(i, numSymbolsValue.intValue());
            }
        }
        return Status.ok();
    }

    @Override
    public EdgebreakerTopology decodeSymbol() {
        // First check if we have a valid context.
        if(activeContext != -1) {
            contextCounters.set(activeContext, val -> val - 1);
            int contextCounter = contextCounters.get(activeContext);
            if(contextCounter < 0) return EdgebreakerTopology.INVALID;

            int symbolId = contextSymbols.get(activeContext).get(contextCounter).intValue();
            if(symbolId > 4) return EdgebreakerTopology.INVALID;

            lastSymbol = EdgebreakerTopology.fromSymbol(symbolId);
        } else if (this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            // We don't have a predicted symbol or the symbol was mis-predicted.
            // Decode it directly.
            lastSymbol = super.decodeSymbol();
        } else {
            // The first symbol must be E.
            lastSymbol = EdgebreakerTopology.E;
        }
        return lastSymbol;
    }

    @Override
    public void newActiveCornerReached(CornerIndex corner) {
        CornerIndex next = cornerTable.next(corner);
        CornerIndex prev = cornerTable.previous(corner);
        // Update valences.
        switch(lastSymbol) {
            case C:
            case S:
                vertexValences.set(cornerTable.getVertex(next), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 1);
                break;
            case R:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 2);
                break;
            case L:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 1);
                break;
            case E:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 2);
                break;
            default:
                break;
        }
        // Compute the new context that is going to be used to decode the next symbol.
        int activeValence = vertexValences.get(cornerTable.getVertex(next));
        int clampedValence = Math.min(Math.max(activeValence, minValence), maxValence);
        activeContext = clampedValence - minValence;
    }

    @Override
    public void mergeVertices(VertexIndex dest, VertexIndex source) {
        // Update valences on the merged vertices.
        vertexValences.set(dest, val -> val + vertexValences.get(source));
    }
}
