package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
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
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.*;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MeshEdgebreakerDecoderImpl implements MeshEdgebreakerDecoderImplInterface {

    @ToString
    private static class AttributeData {
        public int decoderId = -1;
        public MeshAttributeCornerTable connectivityData = new MeshAttributeCornerTable();
        public boolean isConnectivityUsed = true;
        public MeshAttributeIndicesEncodingData encodingData = new MeshAttributeIndicesEncodingData();
        public final CppVector<Integer> attributeSeamCorners = new CppVector<>(DataType.int32());
    }

    private MeshEdgebreakerDecoder decoder = null;

    private CornerTable cornerTable = null;

    /** Stack used for storing corners that need to be traversed when decoding mesh vertices. */
    private final CppVector<CornerIndex> cornerTraversalStack = new CppVector<>(CornerIndex.type());

    /** Array stores the number of visited visited for each mesh traversal. */
    private final CppVector<Integer> vertexTraversalLength = new CppVector<>(DataType.int32());

    /** List of decoded topology split events. */
    private final CppVector<TopologySplitEventData> topologySplitData = new CppVector<>(TopologySplitEventData::new);

    /** List of decoded hole events. */
    // Side note: since struct HoleEventData only has a single int32_t property,
    //            we'll instead store Integer typed values.
    private final CppVector<Integer> holeEventData = new CppVector<>(DataType.int32());

    /** Configuration of the initial face for each mesh component. */
    private final CppVector<Boolean> initFaceConfigurations = new CppVector<>(DataType.bool());

    /** Initial corner for each traversal. */
    private final CppVector<CornerIndex> initCorners = new CppVector<>(CornerIndex.type());

    /** Id of the last processed input symbol. */
    private int lastSymbolId = -1;
    /** Id of the last decoded vertex. */
    private int lastVertexId = -1;
    /** Id of the last decoded face. */
    private int lastFaceId = -1;

    /** Array for marking visited faces. */
    private final CppVector<Boolean> visitedFaces = new CppVector<>(DataType.bool());
    /** Array for marking visited vertices. */
    private final CppVector<Boolean> visitedVertices = new CppVector<>(DataType.bool());
    /** Array for marking vertices on open boundaries. */
    private final CppVector<Boolean> isVertexHole = new CppVector<>(DataType.bool());

    /** The number of new vertices added by the encoder. */
    private int numNewVertices = 0;
    /** For every newly added vertex, this array stores it's mapping to the parent vertex id of the encoded mesh. */
    private final Map<Integer, Integer> newToParentVertexMap = new HashMap<>();
    /** The number of vertices that were encoded. */
    private int numEncodedVertices = 0;

    /** Array for storing the encoded corner ids in the order their associated vertices were decoded. */
    private final CppVector<Integer> processedCornerIds = new CppVector<>(DataType.int32());

    /** Array storing corners in the order they were visited during the connectivity decoding. */
    private final CppVector<Integer> processedConnectivityCorners = new CppVector<>(DataType.int32());

    private final MeshAttributeIndicesEncodingData posEncodingData = new MeshAttributeIndicesEncodingData();

    /** Id of an attributes decoder that uses {@link #posEncodingData}. */
    private int posDataDecoderId = -1;

    private final CppVector<AttributeData> attributeData = new CppVector<>(AttributeData::new);

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

        Pointer<Byte> attDataIdRef = Pointer.newByte();
        if(decoder.getBuffer().decode(attDataIdRef).isError(chain)) return chain.get();
        int attDataId = attDataIdRef.get();

        Pointer<UByte> decoderTypeRef = Pointer.newUByte();
        if(decoder.getBuffer().decode(decoderTypeRef).isError(chain)) return chain.get();
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
            Pointer<UByte> traversalMethodEncodedRef = Pointer.newUByte();
            if(decoder.getBuffer().decode(traversalMethodEncodedRef).isError(chain)) return chain.get();
            MeshTraversalMethod traversalMethodEncoded = MeshTraversalMethod.valueOf(traversalMethodEncodedRef.get());
            if(traversalMethodEncoded == null) {
                return Status.ioError("Decoded traversal method is invalid: " + traversalMethodEncodedRef.get());
            }
            traversalMethod = traversalMethodEncoded;
        }

        Mesh mesh = decoder.getMesh();
        PointsSequencer sequencer;

        if(decoderType == MeshAttributeElementType.VERTEX) {
            // Per-vertex attribute decoder.

            MeshAttributeIndicesEncodingData encodingData;
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
            Pointer<UInt> numNewVertsRef = Pointer.newUInt();
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(buffer.decode(numNewVertsRef).isError(chain)) return chain.get();
            } else {
                if(buffer.decodeVarint(numNewVertsRef).isError(chain)) return chain.get();
            }
            this.numNewVertices = numNewVertsRef.get().intValue();
        }

        Pointer<UInt> numEncodedVerticesRef = Pointer.newUInt();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(numEncodedVerticesRef).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(numEncodedVerticesRef).isError(chain)) return chain.get();
        }
        this.numEncodedVertices = numEncodedVerticesRef.get().intValue();

        Pointer<UInt> numFacesRef = Pointer.newUInt();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(numFacesRef).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(numFacesRef).isError(chain)) return chain.get();
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

        Pointer<UByte> numAttributeDataRef = Pointer.newUByte();
        if(buffer.decode(numAttributeDataRef).isError(chain)) return chain.get();
        int numAttributeData = numAttributeDataRef.get().intValue();

        Pointer<UInt> numEncodedSymbolsRef = Pointer.newUInt();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(numEncodedSymbolsRef).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(numEncodedSymbolsRef).isError(chain)) return chain.get();
        }
        int numEncodedSymbols = numEncodedSymbolsRef.get().intValue();

        if(numFaces < numEncodedSymbols) {
            return Status.ioError("Number of faces needs to be the same or greater than the number of symbols, " +
                    "instead got: " + numFaces + " < " + numEncodedSymbols);
        }
        int maxEncodedFaces = numEncodedSymbols + (numEncodedSymbols / 3);
        if(numFaces > maxEncodedFaces) {
            return Status.ioError("Faces can only be 1 1/3 times bigger than number of encoded symbols, " +
                    "instead got: " + numFaces + " > " + maxEncodedFaces);
        }

        Pointer<UInt> numEncodedSplitSymbolsRef = Pointer.newUInt();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(buffer.decode(numEncodedSplitSymbolsRef).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(numEncodedSplitSymbolsRef).isError(chain)) return chain.get();
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

        if(this.cornerTable.reset(
                numFaces, this.numEncodedVertices + numEncodedSplitSymbols).isError(chain)) return chain.get();

        // Start with all vertices marked as holes (boundaries).
        isVertexHole.assign(this.numEncodedVertices + numEncodedSplitSymbols, true);

        int topologySplitDecodedBytes = -1;
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            Pointer<UInt> encodedConnectivitySizeRef = Pointer.newUInt();
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(buffer.decode(encodedConnectivitySizeRef).isError(chain)) return chain.get();
            } else {
                if(buffer.decodeVarint(encodedConnectivitySizeRef).isError(chain)) return chain.get();
            }
            int encodedConnectivitySize = encodedConnectivitySizeRef.get().intValue();

            if(encodedConnectivitySize == 0 || encodedConnectivitySize > buffer.getRemainingSize()) {
                return Status.ioError("Invalid encoded connectivity size: " + encodedConnectivitySize);
            }
            DecoderBuffer eventBuffer = new DecoderBuffer();
            eventBuffer.init(
                    buffer.getDataHead().rawAdd(encodedConnectivitySize),
                    buffer.getRemainingSize() - encodedConnectivitySize,
                    buffer.getBitstreamVersion());
            StatusOr<Integer> topologySplitDecodedBytesOrError = this.decodeHoleAndTopologySplitEvents(eventBuffer);
            if(topologySplitDecodedBytesOrError.isError(chain)) return chain.get();
            topologySplitDecodedBytes = topologySplitDecodedBytesOrError.getValue();
        } else {
            if(this.decodeHoleAndTopologySplitEvents(buffer).isError(chain)) return chain.get();
        }

        this.traversalDecoder.init(this);
        this.traversalDecoder.setNumEncodedVertices(this.numEncodedVertices + numEncodedSplitSymbols);
        this.traversalDecoder.setNumAttributeData(numAttributeData);

        DecoderBuffer traversalEndBuffer = new DecoderBuffer();
        if(this.traversalDecoder.start(traversalEndBuffer).isError(chain)) return chain.get();

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

        if(!attributeData.isEmpty()) {
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
        CppVector<CornerIndex> activeCornerStack = new CppVector<>(CornerIndex.type());

        Map<Integer, CornerIndex> topologySplitActiveCorners = new HashMap<>();

        CppVector<VertexIndex> invalidVertices = new CppVector<>(VertexIndex.type());
        boolean removeInvalidVertices = this.attributeData.isEmpty();

        int maxNumVertices = (int) isVertexHole.size();
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
        // If any vertex was marked as isolated, we want to remove it from the corner
        // table to ensure that all vertices in range [0, num_vertices) are valid.
        for(VertexIndex invalidVert : invalidVertices) {
            VertexIndex srcVert = VertexIndex.of(numVertices - 1);
            while(cornerTable.getLeftMostCorner(srcVert).isInvalid()) {
                srcVert = VertexIndex.of(--numVertices - 1);
            }
            if(srcVert.getValue() < invalidVert.getValue()) {
                continue; // No need to swap anything
            }

            for(CornerIndex cid : VertexCornersIterator.iterable(cornerTable, srcVert)) {
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

        Pointer<UInt> numTopologySplitsRef = Pointer.newUInt();
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(decoderBuffer.decode(numTopologySplitsRef).isError(chain)) return StatusOr.error(chain);
        } else {
            if(decoderBuffer.decodeVarint(numTopologySplitsRef).isError(chain)) return StatusOr.error(chain);
        }
        int numTopologySplits = numTopologySplitsRef.get().intValue();
        if(numTopologySplits > 0) {
            if(numTopologySplits > cornerTable.getNumFaces()) {
                return StatusOr.ioError("Number of topology splits is greater than number of faces");
            }
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 2)) {
                for(int i = 0; i < numTopologySplits; i++) {
                    TopologySplitEventData eventData = new TopologySplitEventData();

                    Pointer<UInt> splitSymbolIdRef = Pointer.newUInt();
                    if(decoderBuffer.decode(splitSymbolIdRef).isError(chain)) return StatusOr.error(chain);
                    eventData.setSplitSymbolId(splitSymbolIdRef.get());

                    Pointer<UInt> sourceSymbolIdRef = Pointer.newUInt();
                    if(decoderBuffer.decode(sourceSymbolIdRef).isError(chain)) return StatusOr.error(chain);
                    eventData.setSourceSymbolId(sourceSymbolIdRef.get());

                    Pointer<UByte> edgeDataRef = Pointer.newUByte();
                    if(decoderBuffer.decode(edgeDataRef).isError(chain)) return StatusOr.error(chain);
                    eventData.setSourceEdge(EdgeFaceName.valueOf(edgeDataRef.get()));

                    topologySplitData.pushBack(eventData);
                }
            } else {
                int lastSourceSymbolId = 0;
                for(int i = 0; i < numTopologySplits; i++) {
                    TopologySplitEventData eventData = new TopologySplitEventData();

                    Pointer<UInt> deltaRef = Pointer.newUInt();
                    if(decoderBuffer.decodeVarint(deltaRef).isError(chain)) return StatusOr.error(chain);
                    eventData.setSourceSymbolId(deltaRef.get().add(lastSourceSymbolId));

                    if(decoderBuffer.decodeVarint(deltaRef).isError(chain)) return StatusOr.error(chain);
                    if(deltaRef.get().gt(eventData.getSourceSymbolId())) {
                        return StatusOr.ioError("Delta is greater than source symbol id");
                    }
                    eventData.setSplitSymbolId(eventData.getSourceSymbolId().sub(deltaRef.get()));
                    lastSourceSymbolId = eventData.getSourceSymbolId().intValue();
                    topologySplitData.pushBack(eventData);
                }
                decoderBuffer.startBitDecoding(false, Pointer.newULong());
                for(int i = 0; i < numTopologySplits; i++) {
                    Pointer<UInt> edgeDataRef = Pointer.newUInt();
                    if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
                        if(decoderBuffer.decodeLeastSignificantBits32(2, edgeDataRef).isError(chain)) return StatusOr.error(chain);
                    } else {
                        if(decoderBuffer.decodeLeastSignificantBits32(1, edgeDataRef).isError(chain)) return StatusOr.error(chain);
                    }
                    TopologySplitEventData eventData = topologySplitData.get(i);
                    eventData.setSourceEdge(EdgeFaceName.valueOf(edgeDataRef.get().intValue() & 1));
                }
                decoderBuffer.endBitDecoding();
            }
        }
        Pointer<UInt> numHoleEventsRef = Pointer.newUInt(0);
        if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(decoderBuffer.decode(numHoleEventsRef).isError(chain)) return StatusOr.error(chain);
        } else if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 1)) {
            if(decoderBuffer.decodeVarint(numHoleEventsRef).isError(chain)) return StatusOr.error(chain);
        }
        int numHoleEvents = numHoleEventsRef.get().intValue();
        if(numHoleEvents > 0) {
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 2)) {
                for(int i = 0; i < numHoleEvents; i++) {
                    Pointer<Integer> eventDataRef = Pointer.newInt();
                    if(decoderBuffer.decode(eventDataRef).isError(chain)) return StatusOr.error(chain);
                    holeEventData.pushBack(eventDataRef.get());
                }
            } else {
                int lastSymbolId = 0;
                for(int i = 0; i < numHoleEvents; i++) {
                    int eventData;
                    Pointer<UInt> deltaRef = Pointer.newUInt();
                    if(decoderBuffer.decodeVarint(deltaRef).isError(chain)) return StatusOr.error(chain);
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

        CppVector<Integer> pointToCornerMap = new CppVector<>(DataType.int32());
        CppVector<Integer> cornerToPointMap = new CppVector<>(DataType.int32(), cornerTable.getNumCorners());
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
            cornerToPointMap.set(c.getValue(), (int) pointToCornerMap.size());
            pointToCornerMap.pushBack(c.getValue());
            CornerIndex prevC = c;
            c = cornerTable.swingRight(c);
            while(c.isValid() && !c.equals(deduplicationFirstCorner)) {
                boolean attributeSeam = false;
                for(AttributeData data : attributeData) {
                    VertexIndex vertex = data.connectivityData.getVertex(c);
                    VertexIndex prevVertex = data.connectivityData.getVertex(prevC);
                    if(!vertex.equals(prevVertex)) {
                        attributeSeam = true;
                        break;
                    }
                }
                if(attributeSeam) {
                    cornerToPointMap.set(c.getValue(), (int) pointToCornerMap.size());
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
                face.set(c, PointIndex.of(cornerToPointMap.get(3L * f.getValue() + c)));
            }
            decoder.getMesh().setFace(f, face);
        }
        decoder.getPointCloud().setNumPoints((int) pointToCornerMap.size());
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
