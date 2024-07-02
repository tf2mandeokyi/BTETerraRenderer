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
#ifndef DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_IMPL_H_
#define DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_IMPL_H_

#include <unordered_map>
#include <unordered_set>

#include "draco/compression/attributes/mesh_attribute_indices_encoding_data.h"
#include "draco/compression/mesh/mesh_edgebreaker_decoder_impl_interface.h"
#include "draco/compression/mesh/mesh_edgebreaker_shared.h"
#include "draco/compression/mesh/traverser/mesh_traversal_sequencer.h"
#include "draco/core/decoder_buffer.h"
#include "draco/draco_features.h"
#include "draco/mesh/corner_table.h"
#include "draco/mesh/mesh_attribute_corner_table.h"

namespace draco {

// Implementation of the edgebreaker decoder that decodes data encoded with the
// MeshEdgebreakerEncoderImpl class. The implementation of the decoder is based
// on the algorithm presented in Isenburg et al'02 "Spirale Reversi: Reverse
// decoding of the Edgebreaker encoding". Note that the encoding is still based
// on the standard edgebreaker method as presented in "3D Compression
// Made Simple: Edgebreaker on a Corner-Table" by Rossignac at al.'01.
// http://www.cc.gatech.edu/~jarek/papers/CornerTableSMI.pdf. One difference is
// caused by the properties of the spirale reversi algorithm that decodes the
// symbols from the last one to the first one. To make the decoding more
// efficient, we encode all symbols in the reverse order, therefore the decoder
// can process them one by one.
// The main advantage of the spirale reversi method is that the partially
// decoded mesh has valid connectivity data at any time during the decoding
// process (valid with respect to the decoded portion of the mesh). The standard
// Edgebreaker decoder used two passes (forward decoding + zipping) which not
// only prevented us from having a valid connectivity but it was also slower.
// The main benefit of having the valid connectivity is that we can use the
// known connectivity to predict encoded symbols that can improve the
// compression rate.
template <class TraversalDecoderT>
class MeshEdgebreakerDecoderImpl : public MeshEdgebreakerDecoderImplInterface {
 public:
  MeshEdgebreakerDecoderImpl();
  bool Init(MeshEdgebreakerDecoder *decoder) override;

  const MeshAttributeCornerTable *GetAttributeCornerTable(
      int att_id) const override;
  const MeshAttributeIndicesEncodingData *GetAttributeEncodingData(
      int att_id) const override;

  bool CreateAttributesDecoder(int32_t att_decoder_id) override;
  bool DecodeConnectivity() override;
  bool OnAttributesDecoded() override;
  MeshEdgebreakerDecoder *GetDecoder() const override { return decoder_; }
  const CornerTable *GetCornerTable() const override {
    return corner_table_.get();
  }

 private:
  // Creates a vertex traversal sequencer for the specified |TraverserT| type.
  template <class TraverserT>
  std::unique_ptr<PointsSequencer> CreateVertexTraversalSequencer(
      MeshAttributeIndicesEncodingData *encoding_data);

  // Decodes connectivity between vertices (vertex indices).
  // Returns the number of vertices created by the decoder or -1 on error.
  int DecodeConnectivity(int num_symbols);

  // Returns true if the current symbol was part of a topology split event. This
  // means that the current face was connected to the left edge of a face
  // encoded with the TOPOLOGY_S symbol. |out_symbol_edge| can be used to
  // identify which edge of the source symbol was connected to the TOPOLOGY_S
  // symbol.
  bool IsTopologySplit(int encoder_symbol_id, EdgeFaceName *out_face_edge,
                       int *out_encoder_split_symbol_id) {
    if (topology_split_data_.size() == 0) {
      return false;
    }
    if (topology_split_data_.back().source_symbol_id >
        static_cast<uint32_t>(encoder_symbol_id)) {
      // Something is wrong; if the desired source symbol is greater than the
      // current encoder_symbol_id, we missed it, or the input was tampered
      // (|encoder_symbol_id| keeps decreasing).
      // Return invalid symbol id to notify the decoder that there was an
      // error.
      *out_encoder_split_symbol_id = -1;
      return true;
    }
    if (topology_split_data_.back().source_symbol_id != encoder_symbol_id) {
      return false;
    }
    *out_face_edge =
        static_cast<EdgeFaceName>(topology_split_data_.back().source_edge);
    *out_encoder_split_symbol_id = topology_split_data_.back().split_symbol_id;
    // Remove the latest split event.
    topology_split_data_.pop_back();
    return true;
  }

  // Decodes event data for hole and topology split events and stores them for
  // future use.
  // Returns the number of parsed bytes, or -1 on error.
  int32_t DecodeHoleAndTopologySplitEvents(DecoderBuffer *decoder_buffer);

  // Decodes all non-position attribute connectivity on the currently
  // processed face.
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  bool DecodeAttributeConnectivitiesOnFaceLegacy(CornerIndex corner);
#endif
  bool DecodeAttributeConnectivitiesOnFace(CornerIndex corner);

  // Initializes mapping between corners and point ids.
  bool AssignPointsToCorners(int num_connectivity_verts);

  bool IsFaceVisited(CornerIndex corner_id) const {
    if (corner_id < 0) {
      return true;  // Invalid corner signalizes that the face does not exist.
    }
    return visited_faces_[corner_table_->Face(corner_id).value()];
  }

  void SetOppositeCorners(CornerIndex corner_0, CornerIndex corner_1) {
    corner_table_->SetOppositeCorner(corner_0, corner_1);
    corner_table_->SetOppositeCorner(corner_1, corner_0);
  }

  MeshEdgebreakerDecoder *decoder_;

  std::unique_ptr<CornerTable> corner_table_;

  // Stack used for storing corners that need to be traversed when decoding
  // mesh vertices. New corner is added for each initial face and a split
  // symbol, and one corner is removed when the end symbol is reached.
  // Stored as member variable to prevent frequent memory reallocations when
  // handling meshes with lots of disjoint components.  Originally, we used
  // recursive functions to handle this behavior, but that can cause stack
  // memory overflow when compressing huge meshes.
  std::vector<CornerIndex> corner_traversal_stack_;

  // Array stores the number of visited visited for each mesh traversal.
  std::vector<int> vertex_traversal_length_;

  // List of decoded topology split events.
  std::vector<TopologySplitEventData> topology_split_data_;

  // List of decoded hole events.
  std::vector<HoleEventData> hole_event_data_;

  // Configuration of the initial face for each mesh component.
  std::vector<bool> init_face_configurations_;

  // Initial corner for each traversal.
  std::vector<CornerIndex> init_corners_;

  // Id of the last processed input symbol.
  int last_symbol_id_;

  // Id of the last decoded vertex.
  int last_vert_id_;

  // Id of the last decoded face.
  int last_face_id_;

  // Array for marking visited faces.
  std::vector<bool> visited_faces_;
  // Array for marking visited vertices.
  std::vector<bool> visited_verts_;
  // Array for marking vertices on open boundaries.
  std::vector<bool> is_vert_hole_;

  // The number of new vertices added by the encoder (because of non-manifold
  // vertices on the input mesh).
  // If there are no non-manifold edges/vertices on the input mesh, this should
  // be 0.
  int num_new_vertices_;
  // For every newly added vertex, this array stores it's mapping to the
  // parent vertex id of the encoded mesh.
  std::unordered_map<int, int> new_to_parent_vertex_map_;
  // The number of vertices that were encoded (can be different from the number
  // of vertices of the input mesh).
  int num_encoded_vertices_;

  // Array for storing the encoded corner ids in the order their associated
  // vertices were decoded.
  std::vector<int32_t> processed_corner_ids_;

  // Array storing corners in the order they were visited during the
  // connectivity decoding (always storing the tip corner of each newly visited
  // face).
  std::vector<int> processed_connectivity_corners_;

  MeshAttributeIndicesEncodingData pos_encoding_data_;

  // Id of an attributes decoder that uses |pos_encoding_data_|.
  int pos_data_decoder_id_;

  // Data for non-position attributes used by the decoder.
  struct AttributeData {
    AttributeData() : decoder_id(-1), is_connectivity_used(true) {}
    // Id of the attribute decoder that was used to decode this attribute data.
    int decoder_id;
    MeshAttributeCornerTable connectivity_data;
    // Flag that can mark the connectivity_data invalid. In such case the base
    // corner table of the mesh should be used instead.
    bool is_connectivity_used;
    MeshAttributeIndicesEncodingData encoding_data;
    // Opposite corners to attribute seam edges.
    std::vector<int32_t> attribute_seam_corners;
  };
  std::vector<AttributeData> attribute_data_;

  TraversalDecoderT traversal_decoder_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_MESH_MESH_EDGEBREAKER_DECODER_IMPL_H_

 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.SequentialAttributeDecodersController;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.MeshTraversalMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.traverser.*;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.StatusOr;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MeshEdgebreakerDecoderImpl implements MeshEdgebreakerDecoderImplInterface {

    private static class AttributeData {
        public int decoderId = -1;
        public MeshAttributeCornerTable connectivityData = new MeshAttributeCornerTable();
        public boolean isConnectivityUsed = true;
        public MeshAttributeIndicesEncodingData encodingData = new MeshAttributeIndicesEncodingData();
        public final CppVector<Integer> attributeSeamCorners = CppVector.create(DataType.int32());
    }

    private MeshEdgebreakerDecoder decoder = null;

    private CornerTable cornerTable = null;

    /** Stack used for storing corners that need to be traversed when decoding mesh vertices. */
    private final CppVector<CornerIndex> cornerTraversalStack = CppVector.create(CornerIndex.arrayManager());

    /** Array stores the number of visited visited for each mesh traversal. */
    private final CppVector<Integer> vertexTraversalLength = CppVector.create(DataType.int32());

    /** List of decoded topology split events. */
    private final CppVector<TopologySplitEventData> topologySplitData = CppVector.create();

    /** List of decoded hole events. */
    // Side note: since struct HoleEventData only has a single int32_t property,
    //            we'll instead store Integer typed values.
    private final CppVector<Integer> holeEventData = CppVector.create(DataType.int32());

    /** Configuration of the initial face for each mesh component. */
    private final CppVector<Boolean> initFaceConfigurations = CppVector.create(DataType.bool());

    /** Initial corner for each traversal. */
    private final CppVector<CornerIndex> initCorners = CppVector.create(CornerIndex.arrayManager());

    /** Id of the last processed input symbol. */
    private int lastSymbolId = -1;
    /** Id of the last decoded vertex. */
    private int lastVertexId = -1;
    /** Id of the last decoded face. */
    private int lastFaceId = -1;

    /** Array for marking visited faces. */
    private final CppVector<Boolean> visitedFaces = CppVector.create(DataType.bool());
    /** Array for marking visited vertices. */
    private final CppVector<Boolean> visitedVertices = CppVector.create(DataType.bool());
    /** Array for marking vertices on open boundaries. */
    private final CppVector<Boolean> isVertexHole = CppVector.create(DataType.bool());

    /** The number of new vertices added by the encoder. */
    private int numNewVertices = 0;
    /** For every newly added vertex, this array stores it's mapping to the parent vertex id of the encoded mesh. */
    private final Map<Integer, Integer> newToParentVertexMap = new HashMap<>();
    /** The number of vertices that were encoded. */
    private int numEncodedVertices = 0;

    /** Array for storing the encoded corner ids in the order their associated vertices were decoded. */
    private final CppVector<Integer> processedCornerIds = CppVector.create(DataType.int32());

    /** Array storing corners in the order they were visited during the connectivity decoding. */
    private final CppVector<Integer> processedConnectivityCorners = CppVector.create(DataType.int32());

    private final MeshAttributeIndicesEncodingData posEncodingData = new MeshAttributeIndicesEncodingData();

    /** Id of an attributes decoder that uses {@link #posEncodingData}. */
    private int posDataDecoderId = -1;

    private final CppVector<AttributeData> attributeData = CppVector.create();

    private final MeshEdgebreakerTraversalDecoder traversalDecoder;

    public MeshEdgebreakerDecoderImpl(MeshEdgebreakerTraversalDecoder traversalDecoder) {
        this.traversalDecoder = traversalDecoder;
    }

    @Override
    public Status init(MeshEdgebreakerDecoder decoder) {
        this.decoder = decoder;
        return Status.ok();
    }

    @Override
    public MeshAttributeCornerTable getAttributeCornerTable(int attId) {
        for(int i = 0; i < attributeData.size(); ++i) {
            final int decoderId = attributeData.get(i).decoderId;
            if(decoderId < 0 || decoderId >= decoder.getNumAttributesDecoders()) continue;

            final AttributesDecoderInterface dec = decoder.getAttributesDecoder(decoderId);
            for(int j = 0; j < dec.getNumAttributes(); ++j) {
                if(dec.getAttributeId(j) != attId) continue;
                if(!attributeData.get(i).isConnectivityUsed) return null;
                return attributeData.get(i).connectivityData;
            }
        }
        return null;
    }

    @Override
    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId) {
        for(int i = 0; i < attributeData.size(); ++i) {
            final int decoderId = attributeData.get(i).decoderId;
            if(decoderId < 0 || decoderId >= decoder.getNumAttributesDecoders()) continue;

            final AttributesDecoderInterface dec = decoder.getAttributesDecoder(decoderId);
            for(int j = 0; j < dec.getNumAttributes(); ++j) {
                if(dec.getAttributeId(j) != attId) continue;
                return attributeData.get(i).encodingData;
            }
        }
        return posEncodingData;
    }

    @Override
    public Status createAttributesDecoder(int attDecoderId) {
        StatusChain chain = new StatusChain();

        AtomicReference<Byte> attDataIdRef = new AtomicReference<>();
        if(decoder.getBuffer().decode(DataType.int8(), attDataIdRef::set).isError(chain)) return chain.get();
        int attDataId = attDataIdRef.get();

        AtomicReference<UByte> decoderTypeRef = new AtomicReference<>();
        if(decoder.getBuffer().decode(DataType.uint8(), decoderTypeRef::set).isError(chain)) return chain.get();
        MeshAttributeElementType decoderType = MeshAttributeElementType.valueOf(decoderTypeRef.get());

        if(attDataId >= 0) {
            if(attDataId >= attributeData.size()) {
                return Status.ioError("Unexpected attribute data");
            }
            if(attributeData.get(attDataId).decoderId >= 0) {
                return Status.ioError("Attribute data is already mapped to a different attributes decoder");
            }
            attributeData.get(attDataId).decoderId = attDecoderId;
        } else {
            if(posDataDecoderId >= 0) {
                return Status.ioError("Some other decoder is already using the data");
            }
            posDataDecoderId = attDecoderId;
        }

        MeshTraversalMethod traversalMethod = MeshTraversalMethod.DEPTH_FIRST;
        if(decoder.getBitstreamVersion() > DracoVersions.getBitstreamVersion(1, 2)) {
            AtomicReference<UByte> traversalMethodEncodedRef = new AtomicReference<>();
            if(decoder.getBuffer().decode(DataType.uint8(), traversalMethodEncodedRef::set).isError(chain)) return chain.get();
            MeshTraversalMethod traversalMethodEncoded = MeshTraversalMethod.valueOf(traversalMethodEncodedRef.get());
            if(traversalMethodEncoded == null) {
                return Status.ioError("Decoded traversal method is invalid: " + traversalMethodEncodedRef.get());
            }
            traversalMethod = traversalMethodEncoded;
        }

        Mesh mesh = decoder.getMesh();
        PointsSequencer sequencer = null;

        if(decoderType == MeshAttributeElementType.MESH_VERTEX_ATTRIBUTE) {
            // Per-vertex attribute decoder.

            MeshAttributeIndicesEncodingData encodingData = null;
            if(attDataId < 0) {
                encodingData = this.posEncodingData;
            } else {
                encodingData = attributeData.get(attDataId).encodingData;
                attributeData.get(attDataId).isConnectivityUsed = false;
            }
            // Defining sequencer via a traversal scheme.
            if(traversalMethod == MeshTraversalMethod.PREDICTION_DEGREE) {
                sequencer = this.createVertexTraversalSequencer(MeshAttributeIndicesEncodingObserver::new,
                        MaxPredictionDegreeTraverser::new, encodingData);
            }
            else if(traversalMethod == MeshTraversalMethod.DEPTH_FIRST) {
                sequencer = this.createVertexTraversalSequencer(MeshAttributeIndicesEncodingObserver::new,
                        DepthFirstTraverser::new, encodingData);
            }
            else {
                return Status.ioError("Unsupported method: " + traversalMethod);
            }
        } else {
            if(traversalMethod != MeshTraversalMethod.DEPTH_FIRST) {
                return Status.ioError("Unsupported method");
            }
            if(attDataId < 0) {
                return Status.ioError("Attribute data must be specified");
            }

            // Per-corner attribute decoder.

            MeshAttributeIndicesEncodingData encodingData = attributeData.get(attDataId).encodingData;
            MeshAttributeCornerTable cornerTable = attributeData.get(attDataId).connectivityData;

            MeshTraversalSequencer traversalSequencer = new MeshTraversalSequencer(mesh, encodingData);

            MeshAttributeIndicesEncodingObserver attObserver = new MeshAttributeIndicesEncodingObserver();
            attObserver.init(cornerTable, mesh, traversalSequencer, encodingData);

            DepthFirstTraverser attTraverser = new DepthFirstTraverser();
            attTraverser.init(cornerTable, attObserver);

            traversalSequencer.setTraverser(attTraverser);
            sequencer = traversalSequencer;
        }

        if(sequencer == null) {
            return Status.ioError("Failed to create sequencer");
        }

        SequentialAttributeDecodersController attController = new SequentialAttributeDecodersController(sequencer);
        return decoder.setAttributesDecoder(attDecoderId, attController);
    }

    @Override
    public Status decodeConnectivity() {
        StatusChain chain = new StatusChain();
        DecoderBuffer buffer = decoder.getBuffer();

        this.numNewVertices = 0;
        this.newToParentVertexMap.clear();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            AtomicReference<UInt> numNewVertsRef = new AtomicReference<>();
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(buffer.decode(DataType.uint32(), numNewVertsRef::set).isError(chain)) return chain.get();
            } else {
                if(buffer.decodeVarint(DataType.uint32(), numNewVertsRef).isError(chain)) return chain.get();
            }
            this.numNewVertices = numNewVertsRef.get().intValue();
        }

        AtomicReference<UInt> numEncodedVerticesRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint32(), numEncodedVerticesRef::set).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(DataType.uint32(), numEncodedVerticesRef).isError(chain)) return chain.get();
        }
        this.numEncodedVertices = numEncodedVerticesRef.get().intValue();

        AtomicReference<UInt> numFacesRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint32(), numFacesRef::set).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(DataType.uint32(), numFacesRef).isError(chain)) return chain.get();
        }
        int numFaces = numFacesRef.get().intValue();

        if(numFaces > Integer.MAX_VALUE / 3) {
            return Status.ioError("Draco cannot handle this many faces: " + numFaces);
        }

        if(this.numEncodedVertices > numFaces * 3) {
            return Status.ioError("There cannot be more vertices than 3 * num_faces, instead got: "
                    + this.numEncodedVertices);
        }

        int minNumFaceEdges = 3 * numFaces / 2;
        long numEncodedVertices64 = this.numEncodedVertices;
        long maxNumVertexEdges = numEncodedVertices64 * (numEncodedVertices64 - 1) / 2;
        if(maxNumVertexEdges < minNumFaceEdges) {
            return Status.ioError("It is impossible to construct a manifold mesh with these properties");
        }

        AtomicReference<UByte> numAttributeDataRef = new AtomicReference<>();
        if(buffer.decode(DataType.uint8(), numAttributeDataRef::set).isError(chain)) return chain.get();
        int numAttributeData = numAttributeDataRef.get().intValue();

        AtomicReference<UInt> numEncodedSymbolsRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint32(), numEncodedSymbolsRef::set).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(DataType.uint32(), numEncodedSymbolsRef).isError(chain)) return chain.get();
        }
        int numEncodedSymbols = numEncodedSymbolsRef.get().intValue();

        if(numFaces < numEncodedSymbols) {
            return Status.ioError("Number of faces needs to be the same or greater than the number of symbols");
        }
        int maxEncodedFaces = numEncodedSymbols + (numEncodedSymbols / 3);
        if(numFaces > maxEncodedFaces) {
            return Status.ioError("Faces can only be 1 1/3 times bigger than number of encoded symbols");
        }

        AtomicReference<UInt> numEncodedSplitSymbolsRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(DataType.uint32(), numEncodedSplitSymbolsRef::set).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(DataType.uint32(), numEncodedSplitSymbolsRef).isError(chain)) return chain.get();
        }
        int numEncodedSplitSymbols = numEncodedSplitSymbolsRef.get().intValue();

        if(numEncodedSplitSymbols > numEncodedSymbols) {
            return Status.ioError("Split symbols are a sub-set of all symbols");
        }

        // Decode topology (connectivity).
        this.vertexTraversalLength.clear();
        this.cornerTable = new CornerTable();
        this.processedCornerIds.clear();
        this.processedCornerIds.reserve(numFaces);
        this.processedConnectivityCorners.clear();
        this.processedConnectivityCorners.reserve(numFaces);
        this.topologySplitData.clear();
        this.holeEventData.clear();
        this.initFaceConfigurations.clear();
        this.initCorners.clear();

        this.lastSymbolId = -1;
        this.lastFaceId = -1;
        this.lastVertexId = -1;

        this.attributeData.clear();
        // Add one attribute data for each attribute decoder.
        this.attributeData.resize(numAttributeData);

        if(this.cornerTable.reset(numFaces, this.numEncodedVertices + numEncodedSplitSymbols)
                .isError(chain)) return chain.get();

        // Start with all vertices marked as holes (boundaries).
        isVertexHole.assign(this.numEncodedVertices + numEncodedSplitSymbols, true);

        int topologySplitDecodedBytes = -1;
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            AtomicReference<UInt> encodedConnectivitySizeRef = new AtomicReference<>();
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(buffer.decode(DataType.uint32(), encodedConnectivitySizeRef::set).isError(chain)) return chain.get();
            } else {
                if(buffer.decodeVarint(DataType.uint32(), encodedConnectivitySizeRef).isError(chain)) return chain.get();
            }
            int encodedConnectivitySize = encodedConnectivitySizeRef.get().intValue();

            if(encodedConnectivitySize == 0 || encodedConnectivitySize > buffer.getRemainingSize()) {
                return Status.ioError("Invalid encoded connectivity size: " + encodedConnectivitySize);
            }
            DecoderBuffer eventBuffer = new DecoderBuffer();
            eventBuffer.init(
                    buffer.getDataHead().withOffset(encodedConnectivitySize),
                    buffer.getRemainingSize() - encodedConnectivitySize,
                    buffer.getBitstreamVersion());
            StatusOr<Integer> topologySplitDecodedBytesOrError = this.decodeHoleAndTopologySplitEvents(eventBuffer);
            if(topologySplitDecodedBytesOrError.isError(chain)) return chain.get();
            topologySplitDecodedBytes = topologySplitDecodedBytesOrError.getValue();
        } else {
            StatusOr<Integer> topologySplitDecodedBytesOrError = this.decodeHoleAndTopologySplitEvents(buffer);
            if(topologySplitDecodedBytesOrError.isError(chain)) return chain.get();
            topologySplitDecodedBytes = topologySplitDecodedBytesOrError.getValue();
        }

        this.traversalDecoder.init(this);
        this.traversalDecoder.setNumEncodedVertices(this.numEncodedVertices + numEncodedSplitSymbols);
        this.traversalDecoder.setNumAttributeData(numAttributeData);

        AtomicReference<DecoderBuffer> traversalEndBufferRef = new AtomicReference<>();
        if(this.traversalDecoder.start(traversalEndBufferRef).isError(chain)) return chain.get();
        DecoderBuffer traversalEndBuffer = traversalEndBufferRef.get();

        StatusOr<Integer> numConnectivityVertsOrError = this.decodeConnectivity(numEncodedSymbols);
        if(numConnectivityVertsOrError.isError(chain)) return chain.get();
        int numConnectivityVerts = numConnectivityVertsOrError.getValue();

        // Set the main buffer to the end of the traversal.
        decoder.getBuffer().init(
                traversalEndBuffer.getDataHead(),
                traversalEndBuffer.getRemainingSize(),
                decoder.getBuffer().getBitstreamVersion());

        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            // Skip topology split data that was already decoded earlier.
            decoder.getBuffer().advance(topologySplitDecodedBytes);
        }

        if(attributeData.isEmpty()) {
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 1)) {
                for(int ci = 0; ci < cornerTable.getNumCorners(); ci += 3) {
                    CornerIndex corner = CornerIndex.of(ci);
                    if(this.decodeAttributeConnectivitiesOnFaceLegacy(corner).isError(chain)) return chain.get();
                }
            } else {
                for(int ci = 0; ci < cornerTable.getNumCorners(); ci += 3) {
                    CornerIndex corner = CornerIndex.of(ci);
                    if(this.decodeAttributeConnectivitiesOnFace(corner).isError(chain)) return chain.get();
                }
            }
        }
        traversalDecoder.done();

        // Decode attribute connectivity.
        for(AttributeData data : attributeData) {
            data.connectivityData.initEmpty(cornerTable);
            for(int c : data.attributeSeamCorners) {
                data.connectivityData.addSeamEdge(CornerIndex.of(c));
            }
            if(data.connectivityData.recomputeVertices(null, null).isError(chain)) return chain.get();
        }

        posEncodingData.init(cornerTable.getNumVertices());
        for(AttributeData data : attributeData) {
            int attConnectivityVerts = data.connectivityData.getNumVertices();
            if(attConnectivityVerts < cornerTable.getNumVertices()) {
                attConnectivityVerts = cornerTable.getNumVertices();
            }
            data.encodingData.init(attConnectivityVerts);
        }
        return this.assignPointsToCorners(numConnectivityVerts);
    }

    @Override
    public Status onAttributesDecoded() {
        return Status.ok();
    }

    private StatusOr<Integer> decodeConnectivity(int numSymbols) {
        CppVector<CornerIndex> activeCornerStack = CppVector.create(CornerIndex.arrayManager());

        Map<Integer, CornerIndex> topologySplitActiveCorners = new HashMap<>();

        CppVector<VertexIndex> invalidVertices = CppVector.create(VertexIndex.arrayManager());
        boolean removeInvalidVertices = this.attributeData.isEmpty();

        int maxNumVertices = isVertexHole.size();
        int numFaces = 0;
        for(int symbolId = 0; symbolId < numSymbols; ++symbolId) {
            FaceIndex face = FaceIndex.of(numFaces++);
            boolean checkTopologySplit = false;
            EdgebreakerTopology symbol = traversalDecoder.decodeSymbol();
            switch(symbol) {
                case C: {
                    if(activeCornerStack.isEmpty()) {
                        return StatusOr.ioError("Active corner stack is empty");
                    }

                    CornerIndex cornerA = activeCornerStack.popBack();
                    VertexIndex vertexX = cornerTable.getVertex(cornerTable.next(cornerA));
                    CornerIndex cornerB = cornerTable.next(cornerTable.getLeftMostCorner(vertexX));

                    if(cornerA.equals(cornerB)) {
                        return StatusOr.ioError("All matched corners must be different");
                    }
                    if(cornerTable.opposite(cornerA).isValid() || cornerTable.opposite(cornerB).isValid()) {
                        return StatusOr.ioError("One of the corners is already opposite to an existing face");
                    }

                    CornerIndex corner = CornerIndex.of(3 * face.getValue());
                    this.setOppositeCorners(cornerA, corner.add(1));
                    this.setOppositeCorners(cornerB, corner.add(2));

                    VertexIndex vertexAPrev = cornerTable.getVertex(cornerTable.previous(cornerA));
                    VertexIndex vertexBNext = cornerTable.getVertex(cornerTable.next(cornerB));
                    if(vertexX.equals(vertexAPrev) || vertexX.equals(vertexBNext)) {
                        return StatusOr.ioError("Encoding is invalid, because face vertices are degenerate");
                    }
                    cornerTable.mapCornerToVertex(corner, vertexX);
                    cornerTable.mapCornerToVertex(corner.add(1), vertexBNext);
                    cornerTable.mapCornerToVertex(corner.add(2), vertexAPrev);
                    cornerTable.setLeftMostCorner(vertexAPrev, corner.add(2));
                    isVertexHole.set(vertexX.getValue(), false);
                    activeCornerStack.pushBack(corner);
                    break;
                }
                case R: case L: {
                    if(activeCornerStack.isEmpty()) {
                        return StatusOr.ioError("Active corner stack is empty");
                    }

                    CornerIndex cornerA = activeCornerStack.popBack();
                    if(cornerTable.opposite(cornerA).isValid()) {
                        return StatusOr.ioError("Active corner is already opposite to an existing face");
                    }

                    CornerIndex corner = CornerIndex.of(3 * face.getValue());
                    CornerIndex oppCorner, cornerL, cornerR;
                    if(symbol == EdgebreakerTopology.R) {
                        oppCorner = corner.add(2);
                        cornerL = corner.add(1);
                        cornerR = corner;
                    } else {
                        oppCorner = corner.add(1);
                        cornerL = corner;
                        cornerR = corner.add(2);
                    }
                    this.setOppositeCorners(oppCorner, cornerA);
                    VertexIndex newVertIndex = cornerTable.addNewVertex();

                    if(cornerTable.getNumVertices() > maxNumVertices) {
                        return StatusOr.ioError("Unexpected number of decoded vertices");
                    }

                    cornerTable.mapCornerToVertex(oppCorner, newVertIndex);
                    cornerTable.setLeftMostCorner(newVertIndex, oppCorner);

                    VertexIndex vertexR = cornerTable.getVertex(cornerTable.previous(cornerA));
                    cornerTable.mapCornerToVertex(cornerR, vertexR);
                    cornerTable.setLeftMostCorner(vertexR, cornerR);

                    cornerTable.mapCornerToVertex(cornerL, cornerTable.getVertex(cornerTable.next(cornerA)));
                    activeCornerStack.pushBack(corner);
                    checkTopologySplit = true;
                    break;
                }
                case S: {
                    if(activeCornerStack.isEmpty()) {
                        return StatusOr.ioError("Active corner stack is empty");
                    }
                    CornerIndex cornerB = activeCornerStack.popBack();

                    CornerIndex searchResult = topologySplitActiveCorners.get(symbolId);
                    if(searchResult != null) {
                        activeCornerStack.pushBack(searchResult);
                    }
                    if(activeCornerStack.isEmpty()) {
                        return StatusOr.ioError("Active corner stack is empty");
                    }
                    CornerIndex cornerA = activeCornerStack.popBack();

                    if(cornerA.equals(cornerB)) {
                        return StatusOr.ioError("All matched corners must be different");
                    }
                    if(cornerTable.opposite(cornerA).isValid() || cornerTable.opposite(cornerB).isValid()) {
                        return StatusOr.ioError("One of the corners is already opposite to an existing face");
                    }

                    CornerIndex corner = CornerIndex.of(3 * face.getValue());
                    this.setOppositeCorners(cornerA, corner.add(2));
                    this.setOppositeCorners(cornerB, corner.add(1));
                    VertexIndex vertexP = cornerTable.getVertex(cornerTable.previous(cornerA));
                    cornerTable.mapCornerToVertex(corner, vertexP);
                    cornerTable.mapCornerToVertex(corner.add(1), cornerTable.getVertex(cornerTable.next(cornerA)));
                    VertexIndex vertBPrev = cornerTable.getVertex(cornerTable.previous(cornerB));
                    cornerTable.mapCornerToVertex(corner.add(2), vertBPrev);
                    cornerTable.setLeftMostCorner(vertBPrev, corner.add(2));
                    CornerIndex cornerN = cornerTable.next(cornerB);
                    VertexIndex vertexN = cornerTable.getVertex(cornerN);
                    traversalDecoder.mergeVertices(vertexP, vertexN);
                    cornerTable.setLeftMostCorner(vertexP, cornerTable.getLeftMostCorner(vertexN));

                    CornerIndex firstCorner = cornerN;
                    while(cornerN.isValid()) {
                        cornerTable.mapCornerToVertex(cornerN, vertexP);
                        cornerN = cornerTable.swingLeft(cornerN);
                        if(cornerN.equals(firstCorner)) {
                            return StatusOr.ioError("Reached the start again which should not happen for split symbols");
                        }
                    }

                    cornerTable.makeVertexIsolated(vertexN);
                    if(removeInvalidVertices) {
                        invalidVertices.pushBack(vertexN);
                    }
                    activeCornerStack.pushBack(corner);
                    break;
                }
                case E: {
                    CornerIndex corner = CornerIndex.of(3 * face.getValue());
                    VertexIndex firstVertIndex = cornerTable.addNewVertex();
                    cornerTable.mapCornerToVertex(corner, firstVertIndex);
                    cornerTable.mapCornerToVertex(corner.add(1), cornerTable.addNewVertex());
                    cornerTable.mapCornerToVertex(corner.add(2), cornerTable.addNewVertex());

                    if(cornerTable.getNumVertices() > maxNumVertices) {
                        return StatusOr.ioError("Unexpected number of decoded vertices: " + cornerTable.getNumVertices());
                    }

                    cornerTable.setLeftMostCorner(firstVertIndex, corner);
                    cornerTable.setLeftMostCorner(firstVertIndex.add(1), corner.add(1));
                    cornerTable.setLeftMostCorner(firstVertIndex.add(2), corner.add(2));
                    activeCornerStack.pushBack(corner);
                    checkTopologySplit = true;
                    break;
                }
                default: {
                    return StatusOr.ioError("Unknown symbol decoded: " + symbol);
                }
            }

            traversalDecoder.newActiveCornerReached(activeCornerStack.back());

            if(checkTopologySplit) {
                int encoderSymbolId = numSymbols - symbolId - 1;
                AtomicReference<EdgeFaceName> splitEdgeRef = new AtomicReference<>();
                AtomicInteger encoderSplitSymbolIdRef = new AtomicInteger();
                while(this.isTopologySplit(encoderSymbolId, splitEdgeRef, encoderSplitSymbolIdRef)) {
                    EdgeFaceName splitEdge = splitEdgeRef.get();
                    int encoderSplitSymbolId = encoderSplitSymbolIdRef.get();

                    if(encoderSplitSymbolId < 0) {
                        return StatusOr.ioError("Wrong split symbol id: " + encoderSplitSymbolId);
                    }
                    CornerIndex actTopCorner = activeCornerStack.back();
                    CornerIndex newActiveCorner;
                    if(splitEdge == EdgeFaceName.RIGHT) {
                        newActiveCorner = cornerTable.next(actTopCorner);
                    } else {
                        newActiveCorner = cornerTable.previous(actTopCorner);
                    }
                    int decoderSplitSymbolId = numSymbols - encoderSplitSymbolId - 1;
                    topologySplitActiveCorners.put(decoderSplitSymbolId, newActiveCorner);
                }
            }
        }
        if(cornerTable.getNumVertices() > maxNumVertices) {
            return StatusOr.ioError("Unexpected number of decoded vertices: " + cornerTable.getNumVertices());
        }
        while(!activeCornerStack.isEmpty()) {
            CornerIndex corner = activeCornerStack.popBack();
            boolean interiorFace = traversalDecoder.decodeStartFaceConfiguration();
            if(interiorFace) {
                if(numFaces >= cornerTable.getNumFaces()) {
                    return StatusOr.ioError("More faces than expected added to the mesh: " + numFaces);
                }

                VertexIndex vertN = cornerTable.getVertex(cornerTable.next(corner));
                CornerIndex cornerB = cornerTable.next(cornerTable.getLeftMostCorner(vertN));

                VertexIndex vertX = cornerTable.getVertex(cornerTable.next(cornerB));
                CornerIndex cornerC = cornerTable.next(cornerTable.getLeftMostCorner(vertX));

                if(corner.equals(cornerB) || corner.equals(cornerC) || cornerB.equals(cornerC)) {
                    return StatusOr.ioError("All matched corners must be different");
                }
                if (cornerTable.opposite(corner ).isValid() ||
                    cornerTable.opposite(cornerB).isValid() ||
                    cornerTable.opposite(cornerC).isValid()) {
                    return StatusOr.ioError("One of the corners is already opposite to an existing face");
                }

                VertexIndex vertP = cornerTable.getVertex(cornerTable.next(cornerC));

                FaceIndex face = FaceIndex.of(numFaces++);
                CornerIndex newCorner = CornerIndex.of(3 * face.getValue());
                this.setOppositeCorners(newCorner, corner);
                this.setOppositeCorners(newCorner.add(1), cornerB);
                this.setOppositeCorners(newCorner.add(2), cornerC);

                cornerTable.mapCornerToVertex(newCorner, vertX);
                cornerTable.mapCornerToVertex(newCorner.add(1), vertP);
                cornerTable.mapCornerToVertex(newCorner.add(2), vertN);

                for(int ci = 0; ci < 3; ++ci) {
                    isVertexHole.set(cornerTable.getVertex(newCorner.add(ci)).getValue(), false);
                }

                initFaceConfigurations.pushBack(true);
                initCorners.pushBack(newCorner);
            } else {
                initFaceConfigurations.pushBack(false);
                initCorners.pushBack(corner);
            }
        }

        if(numFaces != cornerTable.getNumFaces()) {
            return StatusOr.ioError("Unexpected number of decoded faces: " + numFaces);
        }

        int numVertices = cornerTable.getNumVertices();
        for(VertexIndex invalidVert : invalidVertices) {
            VertexIndex srcVert = VertexIndex.of(numVertices - 1);
            while(cornerTable.getLeftMostCorner(srcVert).isInvalid()) {
                srcVert = VertexIndex.of(--numVertices - 1);
            }
            if(srcVert.getValue() < invalidVert.getValue()) {
                continue; // No need to swap anything
            }

            VertexCornersIterator<?> vcit = new VertexCornersIterator<>(cornerTable, srcVert);
            while(vcit.hasNext()) {
                CornerIndex cid = vcit.next();
                if(!cornerTable.getVertex(cid).equals(srcVert)) {
                    return StatusOr.ioError("Vertex mapped to " + cid + " was not " + srcVert + "." +
                            " This indicates corrupted data");
                }
                cornerTable.mapCornerToVertex(cid, invalidVert);
            }
            cornerTable.setLeftMostCorner(invalidVert, cornerTable.getLeftMostCorner(srcVert));

            cornerTable.makeVertexIsolated(srcVert);
            isVertexHole.set(invalidVert.getValue(), isVertexHole.get(srcVert.getValue()));
            isVertexHole.set(srcVert.getValue(), false);

            numVertices--;
        }
        return StatusOr.ok(numVertices);
    }

    @Override
    public MeshEdgebreakerDecoder getDecoder() {
        return decoder;
    }

    @Override
    public CornerTable getCornerTable() {
        return cornerTable;
    }

    private PointsSequencer createVertexTraversalSequencer(
            Supplier<MeshAttributeIndicesEncodingObserver> attObserverMaker,
            Supplier<? extends TraverserBase> traversalDecoderMaker, MeshAttributeIndicesEncodingData encodingData) {
        Mesh mesh = decoder.getMesh();
        MeshTraversalSequencer traversalSequencer = new MeshTraversalSequencer(mesh, encodingData);

        MeshAttributeIndicesEncodingObserver attObserver = attObserverMaker.get();
        attObserver.init(cornerTable, mesh, traversalSequencer, encodingData);

        TraverserBase traversalDecoder = traversalDecoderMaker.get();
        traversalDecoder.init(cornerTable, attObserver);

        traversalSequencer.setTraverser(traversalDecoder);
        return traversalSequencer;
    }

    private boolean isTopologySplit(int encoderSymbolId,
                                    AtomicReference<EdgeFaceName> outFaceEdge,
                                    AtomicInteger outEncoderSplitSymbolId) {
        if(topologySplitData.isEmpty()) {
            return false;
        }
        if(topologySplitData.back().getSourceSymbolId().gt(encoderSymbolId)) {
            outEncoderSplitSymbolId.set(-1);
            return true;
        }
        if(!topologySplitData.back().getSourceSymbolId().equals(encoderSymbolId)) {
            return false;
        }
        TopologySplitEventData data = topologySplitData.popBack();
        outFaceEdge.set(data.getSourceEdge());
        outEncoderSplitSymbolId.set(data.getSplitSymbolId().intValue());
        return true;
    }

    private StatusOr<Integer> decodeHoleAndTopologySplitEvents(DecoderBuffer decoderBuffer) {
        StatusChain chain = new StatusChain();

        AtomicReference<UInt> numTopologySplitsRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(decoderBuffer.decode(DataType.uint32(), numTopologySplitsRef::set).isError(chain)) return StatusOr.error(chain);
        } else {
            if(decoderBuffer.decodeVarint(DataType.uint32(), numTopologySplitsRef).isError(chain)) return StatusOr.error(chain);
        }
        int numTopologySplits = numTopologySplitsRef.get().intValue();
        if(numTopologySplits > 0) {
            if(numTopologySplits > cornerTable.getNumFaces()) {
                return StatusOr.ioError("Number of topology splits is greater than number of faces");
            }
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 2)) {
                for(int i = 0; i < numTopologySplits; i++) {
                    TopologySplitEventData eventData = new TopologySplitEventData();

                    AtomicReference<UInt> splitSymbolIdRef = new AtomicReference<>();
                    if(decoderBuffer.decode(DataType.uint32(), splitSymbolIdRef::set).isError(chain)) return StatusOr.error(chain);
                    eventData.setSplitSymbolId(splitSymbolIdRef.get());

                    AtomicReference<UInt> sourceSymbolIdRef = new AtomicReference<>();
                    if(decoderBuffer.decode(DataType.uint32(), sourceSymbolIdRef::set).isError(chain)) return StatusOr.error(chain);
                    eventData.setSourceSymbolId(sourceSymbolIdRef.get());

                    AtomicReference<UByte> edgeDataRef = new AtomicReference<>();
                    if(decoderBuffer.decode(DataType.uint8(), edgeDataRef::set).isError(chain)) return StatusOr.error(chain);
                    eventData.setSourceEdge(EdgeFaceName.valueOf(edgeDataRef.get()));

                    topologySplitData.pushBack(eventData);
                }
            } else {
                UInt lastSourceSymbolId = UInt.ZERO;
                for(int i = 0; i < numTopologySplits; i++) {
                    TopologySplitEventData eventData = new TopologySplitEventData();

                    AtomicReference<UInt> deltaRef = new AtomicReference<>();
                    if(decoderBuffer.decodeVarint(DataType.uint32(), deltaRef).isError(chain)) return StatusOr.error(chain);
                    UInt delta = deltaRef.get();
                    eventData.setSourceSymbolId(delta.add(lastSourceSymbolId));

                    if(decoderBuffer.decodeVarint(DataType.uint32(), deltaRef).isError(chain)) return StatusOr.error(chain);
                    if(delta.gt(eventData.getSourceSymbolId())) {
                        return StatusOr.ioError("Delta is greater than source symbol id");
                    }
                    eventData.setSplitSymbolId(eventData.getSourceSymbolId().sub(delta));
                    lastSourceSymbolId = eventData.getSourceSymbolId();
                    topologySplitData.pushBack(eventData);
                }
                decoderBuffer.startBitDecoding(false, val -> {});
                for(int i = 0; i < numTopologySplits; i++) {
                    AtomicReference<UInt> edgeDataRef = new AtomicReference<>();
                    if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
                        if(decoderBuffer.decodeLeastSignificantBits32(2, edgeDataRef::set).isError(chain)) return StatusOr.error(chain);
                    } else {
                        if(decoderBuffer.decodeLeastSignificantBits32(1, edgeDataRef::set).isError(chain)) return StatusOr.error(chain);
                    }
                    EdgeFaceName edgeFaceName = EdgeFaceName.valueOf(edgeDataRef.get().intValue());
                    TopologySplitEventData eventData = topologySplitData.get(i);
                    eventData.setSourceEdge(edgeFaceName);
                }
                decoderBuffer.endBitDecoding();
            }
        }
        AtomicReference<UInt> numHoleEventsRef = new AtomicReference<>();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(decoderBuffer.decode(DataType.uint32(), numHoleEventsRef::set).isError(chain)) return StatusOr.error(chain);
        } else if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 1)) {
            if(decoderBuffer.decodeVarint(DataType.uint32(), numHoleEventsRef).isError(chain)) return StatusOr.error(chain);
        }
        int numHoleEvents = numHoleEventsRef.get().intValue();
        if(numHoleEvents > 0) {
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 2)) {
                for(int i = 0; i < numHoleEvents; i++) {
                    AtomicReference<Integer> eventDataRef = new AtomicReference<>();
                    if(decoderBuffer.decode(DataType.int32(), eventDataRef::set).isError(chain)) return StatusOr.error(chain);
                    holeEventData.pushBack(eventDataRef.get());
                }
            } else {
                int lastSymbolId = 0;
                for(int i = 0; i < numHoleEvents; i++) {
                    int eventData;
                    AtomicReference<UInt> deltaRef = new AtomicReference<>();
                    if(decoderBuffer.decodeVarint(DataType.uint32(), deltaRef).isError(chain)) return StatusOr.error(chain);
                    UInt delta = deltaRef.get();
                    eventData = delta.intValue() + lastSymbolId;
                    lastSymbolId = eventData;
                    holeEventData.pushBack(eventData);
                }
            }
        }
        return StatusOr.ok((int) decoderBuffer.getDecodedSize());
    }

    private Status decodeAttributeConnectivitiesOnFaceLegacy(CornerIndex corner) {
        CornerIndex[] corners = new CornerIndex[] { corner, cornerTable.next(corner), cornerTable.previous(corner) };
        for(CornerIndex cornerIndex : corners) {
            CornerIndex oppCorner = cornerTable.opposite(cornerIndex);
            if(oppCorner.isInvalid()) {
                for(AttributeData data : attributeData) {
                    data.attributeSeamCorners.pushBack(cornerIndex.getValue());
                }
                continue;
            }
            for(int i = 0; i < attributeData.size(); i++) {
                boolean isSeam = traversalDecoder.decodeAttributeSeam(i);
                if(isSeam) {
                    attributeData.get(i).attributeSeamCorners.pushBack(cornerIndex.getValue());
                }
            }
        }
        return Status.ok();
    }

    private Status decodeAttributeConnectivitiesOnFace(CornerIndex corner) {
        CornerIndex[] corners = new CornerIndex[] { corner, cornerTable.next(corner), cornerTable.previous(corner) };

        FaceIndex srcFaceId = cornerTable.getFace(corner);
        for(CornerIndex cornerIndex : corners) {
            CornerIndex oppCorner = cornerTable.opposite(cornerIndex);
            if(oppCorner.isInvalid()) {
                for(AttributeData data : attributeData) {
                    data.attributeSeamCorners.pushBack(cornerIndex.getValue());
                }
                continue;
            }
            FaceIndex oppFaceId = cornerTable.getFace(oppCorner);
            if(oppFaceId.getValue() < srcFaceId.getValue()) {
                continue;
            }
            for(int i = 0; i < attributeData.size(); i++) {
                boolean isSeam = traversalDecoder.decodeAttributeSeam(i);
                if(isSeam) {
                    attributeData.get(i).attributeSeamCorners.pushBack(cornerIndex.getValue());
                }
            }
        }
        return Status.ok();
    }

    private Status assignPointsToCorners(int numConnectivityVerts) {
        decoder.getMesh().setNumFaces(cornerTable.getNumFaces());

        if(attributeData.isEmpty()) {
            for(FaceIndex f : FaceIndex.range(0, decoder.getMesh().getNumFaces())) {
                Mesh.Face face = new Mesh.Face();
                CornerIndex startCorner = CornerIndex.of(3 * f.getValue());
                for(int c = 0; c < 3; ++c) {
                    int vertId = cornerTable.getVertex(startCorner.add(c)).getValue();
                    face.set(c, PointIndex.of(vertId));
                }
                decoder.getMesh().setFace(f, face);
            }
            decoder.getPointCloud().setNumPoints(numConnectivityVerts);
            return Status.ok();
        }

        CppVector<Integer> pointToCornerMap = CppVector.create(DataType.int32());
        CppVector<Integer> cornerToPointMap = CppVector.create(DataType.int32(), cornerTable.getNumCorners());
        for(VertexIndex v : VertexIndex.range(0, cornerTable.getNumVertices())) {
            CornerIndex c = cornerTable.getLeftMostCorner(v);
            if(c.isInvalid()) {
                continue; // Isolated vertex
            }
            CornerIndex deduplicationFirstCorner = c;
            if(!isVertexHole.get(v.getValue())) {
                for(AttributeData data : attributeData) {
                    if(!data.connectivityData.isCornerOnSeam(c)) {
                        continue; // No seam for this attribute, ignore it
                    }
                    VertexIndex vertId = data.connectivityData.getVertex(c);
                    CornerIndex actC = cornerTable.swingRight(c);
                    boolean seamFound = false;
                    while(!actC.equals(c)) {
                        if(actC.isInvalid()) {
                            return Status.ioError("Invalid corner index");
                        }
                        if(!data.connectivityData.getVertex(actC).equals(vertId)) {
                            deduplicationFirstCorner = actC;
                            seamFound = true;
                            break;
                        }
                        actC = cornerTable.swingRight(actC);
                    }
                    if(seamFound) {
                        break;
                    }
                }
            }

            c = deduplicationFirstCorner;
            cornerToPointMap.set(c.getValue(), pointToCornerMap.size());
            pointToCornerMap.pushBack(c.getValue());
            CornerIndex prevC = c;
            c = cornerTable.swingRight(c);
            while(c.isValid() && !c.equals(deduplicationFirstCorner)) {
                boolean attributeSeam = false;
                for(AttributeData data : attributeData) {
                    if(!data.connectivityData.getVertex(c).equals(data.connectivityData.getVertex(prevC))) {
                        attributeSeam = true;
                        break;
                    }
                }
                if(attributeSeam) {
                    cornerToPointMap.set(c.getValue(), pointToCornerMap.size());
                    pointToCornerMap.pushBack(c.getValue());
                } else {
                    cornerToPointMap.set(c.getValue(), cornerToPointMap.get(prevC.getValue()));
                }
                prevC = c;
                c = cornerTable.swingRight(c);
            }
        }
        for(FaceIndex f : FaceIndex.range(0, decoder.getMesh().getNumFaces())) {
            Mesh.Face face = new Mesh.Face();
            for(int c = 0; c < 3; ++c) {
                face.set(c, PointIndex.of(cornerToPointMap.get(3 * f.getValue() + c)));
            }
            decoder.getMesh().setFace(f, face);
        }
        decoder.getPointCloud().setNumPoints(pointToCornerMap.size());
        return Status.ok();
    }

    private boolean isFaceVisited(CornerIndex cornerId) {
        if(cornerId.isInvalid()) {
            return true;
        }
        return visitedFaces.get(cornerTable.getFace(cornerId).getValue());
    }

    private void setOppositeCorners(CornerIndex corner0, CornerIndex corner1) {
        cornerTable.setOppositeCorner(corner0, corner1);
        cornerTable.setOppositeCorner(corner1, corner0);
    }
}
