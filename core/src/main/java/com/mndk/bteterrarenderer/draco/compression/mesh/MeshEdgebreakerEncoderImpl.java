package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.SequentialAttributeEncodersController;
import com.mndk.bteterrarenderer.draco.compression.config.MeshTraversalMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.traverser.*;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeElementType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MeshEdgebreakerEncoderImpl implements MeshEdgebreakerEncoderImplInterface {

    private static class AttributeData {
        private int attributeIndex = -1;
        private final MeshAttributeCornerTable connectivityData = new MeshAttributeCornerTable();
        private boolean isConnectivityUsed = true;
        private final MeshAttributeIndicesEncodingData encodingData = new MeshAttributeIndicesEncodingData();
        private MeshTraversalMethod traversalMethod;
    }

    /** The main encoder that owns this class. */
    @Getter private MeshEdgebreakerEncoder encoder = null;
    /** The mesh that is being encoded. */
    private Mesh mesh = null;
    /** Corner table stores the mesh face connectivity data. */
    @Getter private CornerTable cornerTable = null;
    /** Stack used for storing corners that need to be traversed when encoding the connectivity. */
    private final CppVector<CornerIndex> cornerTraversalStack = new CppVector<>(CornerIndex.type());
    /** Array for marking visited faces. */
    private final CppVector<Boolean> visitedFaces = new CppVector<>(DataType.bool());

    /** Attribute data for position encoding. */
    private final MeshAttributeIndicesEncodingData posEncodingData = new MeshAttributeIndicesEncodingData();

    /** Traversal method used for the position attribute. */
    private MeshTraversalMethod posTraversalMethod;

    /** Array storing corners in the order they were visited during the connectivity encoding. */
    private final CppVector<CornerIndex> processedConnectivityCorners = new CppVector<>(CornerIndex.type());

    /** Array for storing visited vertex ids of all input vertices. */
    private final CppVector<Boolean> visitedVertexIds = new CppVector<>(DataType.bool());

    /** For each traversal, this array stores the number of visited vertices. */
    private final CppVector<Integer> vertexTraversalLength = new CppVector<>(DataType.int32());
    /** Array for storing all topology split events encountered during the mesh traversal. */
    private final CppVector<TopologySplitEventData> topologySplitEventData = new CppVector<>(TopologySplitEventData::new);
    /** Map between faceId and symbolId. */
    private final Map<Integer, Integer> faceToSplitSymbolMap = new HashMap<>();

    /** Array for marking holes that have been reached during the traversal. */
    private final CppVector<Boolean> visitedHoles = new CppVector<>(DataType.bool());
    /** Array for mapping vertices to hole ids. */
    private final CppVector<Integer> vertexHoleId = new CppVector<>(DataType.int32());

    /** Id of the last encoded symbol. */
    private int lastEncodedSymbolId = -1;

    /** The number of encoded split symbols. */
    private UInt numSplitSymbols = UInt.ZERO;

    /** Class vector holding data used for encoding each non-position attribute. */
    private final CppVector<AttributeData> attributeData = new CppVector<>(AttributeData::new);

    /** Array storing mapping between attribute encoder id and attribute data id. */
    private final CppVector<Integer> attributeEncoderToDataIdMap = new CppVector<>(DataType.int32());

    private final MeshEdgebreakerTraversalEncoder traversalEncoder;

    /** If set, the encoder is going to use the same connectivity for all attributes. */
    private boolean useSingleConnectivity = false;

    public MeshEdgebreakerEncoderImpl(MeshEdgebreakerTraversalEncoder traversalEncoder) {
        this.traversalEncoder = traversalEncoder;
    }

    @Override
    public Status init(MeshEdgebreakerEncoder encoder) {
        this.encoder = encoder;
        this.mesh = encoder.getMesh();
        this.attributeEncoderToDataIdMap.clear();

        if(encoder.getOptions().isGlobalOptionSet("split_mesh_on_seams")) {
            this.useSingleConnectivity = encoder.getOptions().getGlobalBool("split_mesh_on_seams", false);
        } else {
            this.useSingleConnectivity = encoder.getOptions().getSpeed() >= 6;
        }
        return Status.ok();
    }

    @Override
    public MeshAttributeCornerTable getAttributeCornerTable(int attId) {
        for(int i = 0; i < attributeData.size(); i++) {
            if(attributeData.get(i).attributeIndex != attId) continue;
            if(!attributeData.get(i).isConnectivityUsed) return null;
            return attributeData.get(i).connectivityData;
        }
        return null;
    }

    @Override
    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId) {
        for(int i = 0; i < attributeData.size(); i++) {
            if(attributeData.get(i).attributeIndex != attId) continue;
            return attributeData.get(i).encodingData;
        }
        return posEncodingData;
    }

    @Override
    public Status generateAttributesEncoder(int attId) {
        if(useSingleConnectivity && this.getEncoder().getNumAttributesEncoders() > 0) {
            this.getEncoder().getAttributesEncoder(0).addAttributeId(attId);
            return Status.ok();
        }
        MeshAttributeElementType elementType = this.getEncoder().getMesh().getAttributeElementType(attId);
        PointAttribute att = this.getEncoder().getPointCloud().getAttribute(attId);
        int attDataId = -1;
        for(int i = 0; i < attributeData.size(); i++) {
            if(attributeData.get(i).attributeIndex != attId) continue;
            attDataId = i;
            break;
        }
        MeshTraversalMethod traversalMethod = MeshTraversalMethod.DEPTH_FIRST;
        PointsSequencer sequencer;
        if(useSingleConnectivity
                || att.getAttributeType() == GeometryAttribute.Type.POSITION
                || elementType == MeshAttributeElementType.VERTEX
                || (elementType == MeshAttributeElementType.CORNER
                    && attributeData.get(attDataId).connectivityData.isNoInteriorSeams()
                )) {
            // Per-vertex attribute reached, use the basic corner table to traverse the mesh.
            MeshAttributeIndicesEncodingData encodingData;
            if(useSingleConnectivity || att.getAttributeType() == GeometryAttribute.Type.POSITION) {
                encodingData = posEncodingData;
            } else {
                encodingData = attributeData.get(attDataId).encodingData;

                // Ensure we use the correct number of vertices in the encoding data.
                encodingData.getVertexToEncodedAttributeValueIndexMap().assign(cornerTable.getNumVertices(), -1);

                // Mark the attribute specific connectivity data as not used.
                attributeData.get(attDataId).isConnectivityUsed = false;
            }

            if(this.getEncoder().getOptions().getSpeed() == 0 &&
                    att.getAttributeType() == GeometryAttribute.Type.POSITION) {
                traversalMethod = MeshTraversalMethod.PREDICTION_DEGREE;
                if(useSingleConnectivity && mesh.getNumAttributes() > 1) {
                    // Make sure we don't use the prediction degree traversal when we encode
                    // multiple attributes using the same connectivity.
                    traversalMethod = MeshTraversalMethod.DEPTH_FIRST;
                }
            }
            // Defining sequencer via a traversal scheme.
            if(traversalMethod == MeshTraversalMethod.PREDICTION_DEGREE) {
                sequencer = createVertexTraversalSequencer(new MaxPredictionDegreeTraverser(), encodingData);
            } else /* if(traversalMethod == MeshTraversalMethod.DEPTH_FIRST) */ {
                sequencer = createVertexTraversalSequencer(new DepthFirstTraverser(), encodingData);
            }
        }
        else {
            // Per-corner attribute encoder.
            DepthFirstTraverser attTraverser = new DepthFirstTraverser();
            MeshAttributeIndicesEncodingData encodingData = attributeData.get(attDataId).encodingData;
            MeshAttributeCornerTable cornerTable = attributeData.get(attDataId).connectivityData;

            // Ensure we use the correct number of vertices in the encoding data.
            encodingData.getVertexToEncodedAttributeValueIndexMap().assign(cornerTable.getNumVertices(), -1);

            MeshTraversalSequencer traversalSequencer = new MeshTraversalSequencer(mesh, encodingData);
            MeshAttributeIndicesEncodingObserver attObserver = new MeshAttributeIndicesEncodingObserver();
            attObserver.init(cornerTable, mesh, traversalSequencer, encodingData);
            attTraverser.init(cornerTable, attObserver);

            traversalSequencer.setCornerOrder(processedConnectivityCorners);
            traversalSequencer.setTraverser(attTraverser);
            sequencer = traversalSequencer;
        }

        if(attDataId == -1) {
            posTraversalMethod = traversalMethod;
        } else {
            attributeData.get(attDataId).traversalMethod = traversalMethod;
        }

        SequentialAttributeEncodersController attController = new SequentialAttributeEncodersController(sequencer, attId);

        attributeEncoderToDataIdMap.pushBack(attDataId);
        this.getEncoder().addAttributesEncoder(attController);
        return Status.ok();
    }

    @Override
    public Status encodeAttributesEncoderIdentifier(int attEncoderId) {
        int attDataId = attributeEncoderToDataIdMap.get(attEncoderId);
        encoder.getBuffer().encode(DataType.int8(), (byte) attDataId);

        // Also encode the type of the encoder that we used.
        MeshAttributeElementType elementType = MeshAttributeElementType.VERTEX;
        MeshTraversalMethod traversalMethod;
        if(attDataId >= 0) {
            int attId = attributeData.get(attDataId).attributeIndex;
            elementType = mesh.getAttributeElementType(attId);
            traversalMethod = attributeData.get(attDataId).traversalMethod;
        } else {
            traversalMethod = posTraversalMethod;
        }

        if(elementType == MeshAttributeElementType.VERTEX || (elementType == MeshAttributeElementType.CORNER
                    && attributeData.get(attDataId).connectivityData.isNoInteriorSeams())) {
            // Per-vertex encoder.
            encoder.getBuffer().encode(DataType.uint8(), UByte.of(MeshAttributeElementType.VERTEX.getValue()));
        } else {
            // Per-corner encoder.
            encoder.getBuffer().encode(DataType.uint8(), UByte.of(MeshAttributeElementType.CORNER.getValue()));
        }
        // Encode the mesh traversal method.
        encoder.getBuffer().encode(DataType.uint8(), UByte.of(traversalMethod.getValue()));
        return Status.ok();
    }

    @Override
    public Status encodeConnectivity() {
        StatusChain chain = new StatusChain();

        // To encode the mesh, we need face connectivity data stored in a corner table.
        if(useSingleConnectivity) {
            cornerTable = MeshUtil.createCornerTableFromAllAttributes(mesh);
        } else {
            cornerTable = MeshUtil.createCornerTableFromPositionAttribute(mesh);
        }
        if (cornerTable == null) {
            // Failed to construct the corner table.
            return Status.dracoError("Corner table does not exist");
        } else if (cornerTable.getNumFaces() == cornerTable.getNumDegeneratedFaces()) {
            // Failed to construct the corner table.
            return Status.dracoError("All " + cornerTable.getNumFaces() + " triangles are degenerate.");
        }

        traversalEncoder.init(this);

        // Also encode the total number of vertices that is going to be encoded.
        int numVerticesToBeEncoded = cornerTable.getNumVertices() - cornerTable.getNumIsolatedVertices();
        encoder.getBuffer().encodeVarint(UInt.of(numVerticesToBeEncoded));

        int numFaces = cornerTable.getNumFaces() - cornerTable.getNumDegeneratedFaces();
        encoder.getBuffer().encodeVarint(UInt.of(numFaces));

        // Reset encoder data that may have been initialized in previous runs.
        visitedFaces.assign(mesh.getNumFaces(), false);
        posEncodingData.getVertexToEncodedAttributeValueIndexMap().assign(cornerTable.getNumVertices(), -1);
        posEncodingData.getEncodedAttributeValueIndexToCornerMap().clear();
        posEncodingData.getEncodedAttributeValueIndexToCornerMap().reserve(cornerTable.getNumFaces() * 3L);
        visitedVertexIds.assign(cornerTable.getNumVertices(), false);
        vertexTraversalLength.clear();
        lastEncodedSymbolId = -1;
        numSplitSymbols = UInt.ZERO;
        topologySplitEventData.clear();
        faceToSplitSymbolMap.clear();
        visitedHoles.clear();
        vertexHoleId.assign(cornerTable.getNumVertices(), -1);
        processedConnectivityCorners.clear();
        processedConnectivityCorners.reserve(cornerTable.getNumFaces());
        posEncodingData.setNumValues(0);

        if(this.findHoles().isError(chain)) return chain.get();
        if(this.initAttributeData().isError(chain)) return chain.get();

        int numAttributeData = (int) attributeData.size();
        encoder.getBuffer().encode(UByte.of(numAttributeData));
        traversalEncoder.setNumAttributeData(numAttributeData);

        int numCorners = cornerTable.getNumCorners();

        traversalEncoder.start();

        CppVector<CornerIndex> initFaceConnectivityCorners = new CppVector<>(CornerIndex.type());
        // Traverse the surface starting from each unvisited corner.
        for(int cId = 0; cId < numCorners; cId++) {
            CornerIndex cornerIndex = CornerIndex.of(cId);
            FaceIndex faceId = cornerTable.getFace(cornerIndex);
            if(visitedFaces.get(faceId.getValue())) {
                continue;  // Face has been already processed.
            }
            if(cornerTable.isDegenerated(faceId)) {
                continue;  // Ignore degenerated faces.
            }

            Pointer<CornerIndex> startCornerRef = CornerIndex.type().newOwned();
            boolean interiorConfig = this.findInitFaceConfiguration(faceId, startCornerRef);
            CornerIndex startCorner = startCornerRef.get();
            traversalEncoder.encodeStartFaceConfiguration(interiorConfig);

            if(interiorConfig) {
                // Select the correct vertex on the face as the root.
                cornerIndex = startCorner;
                VertexIndex vertId = cornerTable.getVertex(cornerIndex);
                // Mark all vertices of a given face as visited.
                VertexIndex nextVertId = cornerTable.getVertex(cornerTable.next(cornerIndex));
                VertexIndex prevVertId = cornerTable.getVertex(cornerTable.previous(cornerIndex));

                visitedVertexIds.set(vertId.getValue(), true);
                visitedVertexIds.set(nextVertId.getValue(), true);
                visitedVertexIds.set(prevVertId.getValue(), true);
                // New traversal started. Initiate its length with the first vertex.
                vertexTraversalLength.pushBack(1);

                // Mark the face as visited.
                visitedFaces.set(faceId.getValue(), true);
                // Start compressing from the opposite face of the "next" corner.
                initFaceConnectivityCorners.pushBack(cornerTable.next(cornerIndex));
                CornerIndex oppId = cornerTable.opposite(cornerTable.next(cornerIndex));
                FaceIndex oppFaceId = cornerTable.getFace(oppId);
                if(oppFaceId.isValid() && !visitedFaces.get(oppFaceId.getValue())) {
                    if(this.encodeConnectivityFromCorner(oppId).isError(chain)) return chain.get();
                }
            } else {
                // Boundary configuration.
                this.encodeHole(cornerTable.next(startCorner), true);
                // Start processing the face opposite to the boundary edge.
                if(this.encodeConnectivityFromCorner(startCorner).isError(chain)) return chain.get();
            }
        }
        // Reverse the order of connectivity corners to match the order in which
        // they are going to be decoded.
        processedConnectivityCorners.reverse();
        // Append the init face connectivity corners.
        for(CornerIndex ci : initFaceConnectivityCorners) {
            processedConnectivityCorners.pushBack(ci);
        }
        // Encode connectivity for all non-position attributes.
        if(!attributeData.isEmpty()) {
            // Use the same order of corner that will be used by the decoder.
            visitedFaces.assign(mesh.getNumFaces(), false);
            for(CornerIndex ci : processedConnectivityCorners) {
                if(this.encodeAttributeConnectivitiesOnFace(ci).isError(chain)) return chain.get();
            }
        }
        traversalEncoder.done();

        // Encode the number of symbols.
        int numEncodedSymbols = traversalEncoder.getNumEncodedSymbols();
        encoder.getBuffer().encodeVarint(UInt.of(numEncodedSymbols));

        // Encode the number of split symbols.
        encoder.getBuffer().encodeVarint(numSplitSymbols);

        // Append the traversal buffer.
        if(this.encodeSplitData().isError(chain)) return chain.get();
        EncoderBuffer buffer = traversalEncoder.getBuffer();
        encoder.getBuffer().encode(DataType.bytes(buffer.size()), buffer.getData());

        return Status.ok();
    }

    @Override
    public boolean isFaceEncoded(int fi) {
        return visitedFaces.get(fi);
    }

    private Status initAttributeData() {
        if(useSingleConnectivity) {
            return Status.ok();  // All attributes use the same connectivity.
        }

        int numAttributes = mesh.getNumAttributes();
        // Ignore the position attribute. It's decoded separately.
        attributeData.resize(numAttributes - 1);
        if(numAttributes == 1) {
            return Status.ok();
        }
        int dataIndex = 0;
        for(int i = 0; i < numAttributes; i++) {
            if(mesh.getAttribute(i).getAttributeType() == GeometryAttribute.Type.POSITION) {
                continue;
            }
            PointAttribute att = mesh.getAttribute(i);
            AttributeData data = attributeData.get(dataIndex);
            data.attributeIndex = i;
            data.encodingData.getEncodedAttributeValueIndexToCornerMap().clear();
            data.encodingData.getEncodedAttributeValueIndexToCornerMap().reserve(cornerTable.getNumCorners());
            data.encodingData.setNumValues(0);
            data.connectivityData.initFromAttribute(mesh, cornerTable, att);
            dataIndex++;
        }
        return Status.ok();
    }

    private PointsSequencer createVertexTraversalSequencer(TraverserBase traverser,
                                                           MeshAttributeIndicesEncodingData encodingData) {
        MeshTraversalSequencer traversalSequencer = new MeshTraversalSequencer(mesh, encodingData);

        MeshAttributeIndicesEncodingObserver attObserver = new MeshAttributeIndicesEncodingObserver();
        attObserver.init(cornerTable, mesh, traversalSequencer, encodingData);
        traverser.init(cornerTable, attObserver);

        traversalSequencer.setCornerOrder(processedConnectivityCorners);
        traversalSequencer.setTraverser(traverser);
        return traversalSequencer;
    }

    private boolean findInitFaceConfiguration(FaceIndex faceId, Pointer<CornerIndex> outCorner) {
        CornerIndex cornerIndex = CornerIndex.of(3 * faceId.getValue());
        for(int i = 0; i < 3; i++) {
            if(cornerTable.opposite(cornerIndex).isInvalid()) {
                outCorner.set(cornerIndex);
                return false;
            }
            if(vertexHoleId.get(cornerTable.getVertex(cornerIndex).getValue()) != -1) {
                CornerIndex rightCorner = cornerIndex;
                while(rightCorner.isValid()) {
                    cornerIndex = rightCorner;
                    rightCorner = cornerTable.swingRight(rightCorner);
                }
                outCorner.set(cornerTable.previous(cornerIndex));
                return false;
            }
            cornerIndex = cornerTable.next(cornerIndex);
        }
        outCorner.set(cornerIndex);
        return true;
    }

    private Status encodeConnectivityFromCorner(CornerIndex cornerId) {
        cornerTraversalStack.clear();
        cornerTraversalStack.pushBack(cornerId);
        int numFaces = mesh.getNumFaces();
        while(!cornerTraversalStack.isEmpty()) {
            // Currently processed corner.
            cornerId = cornerTraversalStack.popBack();
            // Make sure the face hasn't been visited yet.
            if(cornerId.isInvalid() || visitedFaces.get(cornerTable.getFace(cornerId).getValue())) {
                // This face has been already traversed.
                continue;
            }
            int numVisitedFaces = 0;
            while(numVisitedFaces < numFaces) {
                ++numVisitedFaces;
                ++lastEncodedSymbolId;

                FaceIndex faceId = cornerTable.getFace(cornerId);
                visitedFaces.set(faceId.getValue(), true);
                processedConnectivityCorners.pushBack(cornerId);
                traversalEncoder.newCornerReached(cornerId);
                VertexIndex vertId = cornerTable.getVertex(cornerId);
                boolean onBoundary = vertexHoleId.get(vertId.getValue()) != -1;
                if(!isVertexVisited(vertId)) {
                    // A new unvisited vertex has been reached.
                    visitedVertexIds.set(vertId.getValue(), true);
                    if(!onBoundary) {
                        traversalEncoder.encodeSymbol(EdgebreakerTopology.C);
                        cornerId = getRightCorner(cornerId);
                        continue;
                    }
                }
                // The current vertex has been already visited or it was on a boundary.
                CornerIndex rightCornerId = getRightCorner(cornerId);
                CornerIndex leftCornerId = getLeftCorner(cornerId);
                FaceIndex rightFaceId = cornerTable.getFace(rightCornerId);
                FaceIndex leftFaceId = cornerTable.getFace(leftCornerId);
                if(this.isRightFaceVisited(cornerId)) {
                    // Check whether there is a topology split event.
                    if(rightFaceId.isValid()) {
                        checkAndStoreTopologySplitEvent(lastEncodedSymbolId,
                                EdgeFaceName.RIGHT, rightFaceId.getValue());
                    }
                    if(this.isLeftFaceVisited(cornerId)) {
                        // Both neighboring faces are visited. End reached.
                        // Check whether there is a topology split event on the left face.
                        if(leftFaceId.isValid()) {
                            checkAndStoreTopologySplitEvent(lastEncodedSymbolId,
                                    EdgeFaceName.LEFT, leftFaceId.getValue());
                        }
                        traversalEncoder.encodeSymbol(EdgebreakerTopology.E);
                        break;
                    } else {
                        traversalEncoder.encodeSymbol(EdgebreakerTopology.R);
                        cornerId = leftCornerId;
                    }
                } else {
                    // Right face was not visited.
                    if(this.isLeftFaceVisited(cornerId)) {
                        // Check whether there is a topology split event on the left face.
                        if(leftFaceId.isValid()) {
                            checkAndStoreTopologySplitEvent(lastEncodedSymbolId,
                                    EdgeFaceName.LEFT, leftFaceId.getValue());
                        }
                        traversalEncoder.encodeSymbol(EdgebreakerTopology.L);
                        cornerId = rightCornerId;
                    } else {
                        traversalEncoder.encodeSymbol(EdgebreakerTopology.S);
                        numSplitSymbols = numSplitSymbols.add(1);
                        if (onBoundary) {
                            int holeId = vertexHoleId.get(vertId.getValue());
                            if (!visitedHoles.get(holeId)) {
                                this.encodeHole(cornerId, false);
                            }
                        }
                        faceToSplitSymbolMap.put(faceId.getValue(), lastEncodedSymbolId);
                        cornerTraversalStack.pushBack(leftCornerId);
                        cornerTraversalStack.pushBack(rightCornerId);
                        break;
                    }
                }
            }
        }
        return Status.ok();
    }

    private int encodeHole(CornerIndex startCornerId, boolean encodeFirstVertex) {
        CornerIndex cornerId = startCornerId;
        cornerId = cornerTable.previous(cornerId);
        while(cornerTable.opposite(cornerId).isValid()) {
            cornerId = cornerTable.opposite(cornerId);
            cornerId = cornerTable.next(cornerId);
        }
        VertexIndex startVertexId = cornerTable.getVertex(startCornerId);

        int numEncodedHoleVerts = 0;
        if(encodeFirstVertex) {
            visitedVertexIds.set(startVertexId.getValue(), true);
            ++numEncodedHoleVerts;
        }

        // Mark the hole as visited.
        visitedHoles.set(vertexHoleId.get(startVertexId.getValue()), true);
        // Get the start vertex of the edge and use it as a reference.
        // VertexIndex startVertId = cornerTable.getVertex(cornerTable.next(cornerId));
        // Get the end vertex of the edge.
        VertexIndex actVertexId = cornerTable.getVertex(cornerTable.previous(cornerId));
        while(!actVertexId.equals(startVertexId)) {
            // Encode the end vertex of the boundary edge.
            // startVertId = actVertexId;
            // Mark the vertex as visited.
            visitedVertexIds.set(actVertexId.getValue(), true);
            ++numEncodedHoleVerts;
            cornerId = cornerTable.next(cornerId);
            // Look for the next attached open boundary edge.
            while(cornerTable.opposite(cornerId).isValid()) {
                cornerId = cornerTable.opposite(cornerId);
                cornerId = cornerTable.next(cornerId);
            }
            actVertexId = cornerTable.getVertex(cornerTable.previous(cornerId));
        }
        return numEncodedHoleVerts;
    }

    private Status encodeSplitData() {
        int numEvents = (int) topologySplitEventData.size();
        encoder.getBuffer().encodeVarint(UInt.of(numEvents));
        if(numEvents > 0) {
            // Encode split symbols using delta and varint coding.
            int lastSourceSymbolId = 0;
            for(int i = 0; i < numEvents; i++) {
                TopologySplitEventData eventData = topologySplitEventData.get(i);
                encoder.getBuffer().encodeVarint(eventData.getSourceSymbolId().sub(lastSourceSymbolId));
                encoder.getBuffer().encodeVarint(eventData.getSourceSymbolId().sub(eventData.getSplitSymbolId()));
                lastSourceSymbolId = eventData.getSourceSymbolId().intValue();
            }
            encoder.getBuffer().startBitEncoding(numEvents, false);
            for(int i = 0; i < numEvents; i++) {
                TopologySplitEventData eventData = topologySplitEventData.get(i);
                encoder.getBuffer().encodeLeastSignificantBits32(1, UInt.of(eventData.getSourceEdge().getValue()));
            }
            encoder.getBuffer().endBitEncoding();
        }
        return Status.ok();
    }

    private CornerIndex getRightCorner(CornerIndex cornerId) {
        CornerIndex nextCornerId = cornerTable.next(cornerId);
        return cornerTable.opposite(nextCornerId);
    }
    private CornerIndex getLeftCorner(CornerIndex cornerId) {
        CornerIndex prevCornerId = cornerTable.previous(cornerId);
        return cornerTable.opposite(prevCornerId);
    }

    private boolean isRightFaceVisited(CornerIndex cornerId) {
        CornerIndex nextCornerId = cornerTable.next(cornerId);
        CornerIndex oppCornerId = cornerTable.opposite(nextCornerId);
        if(oppCornerId.isValid()) {
            return visitedFaces.get(cornerTable.getFace(oppCornerId).getValue());
        }
        return true;
    }
    private boolean isLeftFaceVisited(CornerIndex cornerId) {
        CornerIndex prevCornerId = cornerTable.previous(cornerId);
        CornerIndex oppCornerId = cornerTable.opposite(prevCornerId);
        if(oppCornerId.isValid()) {
            return visitedFaces.get(cornerTable.getFace(oppCornerId).getValue());
        }
        return true;
    }
    private boolean isVertexVisited(VertexIndex vertId) {
        return visitedVertexIds.get(vertId.getValue());
    }

    private Status findHoles() {
        int numCorners = cornerTable.getNumCorners();
        // Go over all corners and detect non-visited open boundaries
        for(CornerIndex i : CornerIndex.range(0, numCorners)) {
            if(cornerTable.isDegenerated(cornerTable.getFace(i))) {
                continue;  // Don't process corners assigned to degenerated faces.
            }
            if(cornerTable.opposite(i).isInvalid()) {
                // Check whether we have already traversed the boundary.
                VertexIndex boundaryVertId = cornerTable.getVertex(cornerTable.next(i));
                if(vertexHoleId.get(boundaryVertId.getValue()) != -1) {
                    // No need to traverse it again.
                    continue;
                }
                // Traverse along the new boundary and mark all visited vertices.
                int boundaryId = (int) visitedHoles.size();
                visitedHoles.pushBack(false);

                CornerIndex cornerId = i;
                while(vertexHoleId.get(boundaryVertId.getValue()) == -1) {
                    // Mark the first vertex on the open boundary.
                    vertexHoleId.set(boundaryVertId.getValue(), boundaryId);
                    cornerId = cornerTable.next(cornerId);
                    // Look for the next attached open boundary edge.
                    while(cornerTable.opposite(cornerId).isValid()) {
                        cornerId = cornerTable.opposite(cornerId);
                        cornerId = cornerTable.next(cornerId);
                    }
                    // Id of the next vertex in the vertex on the hole.
                    boundaryVertId = cornerTable.getVertex(cornerTable.next(cornerId));
                }
            }
        }
        return Status.ok();
    }

    private int getSplitSymbolIdOnFace(int faceId) {
        return faceToSplitSymbolMap.getOrDefault(faceId, -1);
    }

    private void checkAndStoreTopologySplitEvent(int srcSymbolId, EdgeFaceName srcEdge, int neighborFaceId) {
        int symbolId = this.getSplitSymbolIdOnFace(neighborFaceId);
        if(symbolId == -1) return; // Not a split symbol, no topology split event could happen.

        TopologySplitEventData eventData = new TopologySplitEventData();
        eventData.setSplitSymbolId(UInt.of(symbolId));
        eventData.setSourceSymbolId(UInt.of(srcSymbolId));
        eventData.setSourceEdge(srcEdge);
        topologySplitEventData.pushBack(eventData);
    }

    private Status encodeAttributeConnectivitiesOnFace(CornerIndex corner) {
        CornerIndex[] corners = new CornerIndex[] {
                corner, cornerTable.next(corner), cornerTable.previous(corner)
        };
        FaceIndex srcFaceId = cornerTable.getFace(corner);
        visitedFaces.set(srcFaceId.getValue(), true);
        for(int c = 0; c < 3; ++c) {
            CornerIndex oppCorner = cornerTable.opposite(corners[c]);
            if(oppCorner.isInvalid()) {
                continue; // Don't encode attribute seams on boundary edges.
            }
            FaceIndex oppFaceId = cornerTable.getFace(oppCorner);
            // Don't encode edges when the opposite face has been already processed.
            if(visitedFaces.get(oppFaceId.getValue())) {
                continue;
            }

            for(int i = 0; i < attributeData.size(); ++i) {
                boolean encodeSeam = attributeData.get(i).connectivityData.isCornerOppositeToSeamEdge(corners[c]);
                traversalEncoder.encodeAttributeSeam(i, encodeSeam);
            }
        }
        return Status.ok();
    }

    private Status assignPositionEncodingOrderToAllCorners() {
        return Status.unsupportedFeature("assignPositionEncodingOrderToAllCorners");
    }

    private Status generateEncodingOrderForAttributes() {
        return Status.unsupportedFeature("generateEncodingOrderForAttributes");
    }
}
