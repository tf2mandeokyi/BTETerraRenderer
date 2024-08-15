package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CornerTable implements ICornerTable {

    public static class FaceType {
        public final VertexIndex[] vertices = new VertexIndex[3];

        public FaceType() {
            this(VertexIndex.INVALID, VertexIndex.INVALID, VertexIndex.INVALID);
        }
        public FaceType(VertexIndex v0, VertexIndex v1, VertexIndex v2) {
            vertices[0] = v0;
            vertices[1] = v1;
            vertices[2] = v2;
        }
        public VertexIndex get(int i) {
            return vertices[i];
        }
        public void set(int i, VertexIndex v) {
            vertices[i] = v;
        }
    }

    private final IndexTypeVector<CornerIndex, VertexIndex> cornerToVertexMap =
            new IndexTypeVector<>(VertexIndex.type());
    private final IndexTypeVector<CornerIndex, CornerIndex> oppositeCorners =
            new IndexTypeVector<>(CornerIndex.type());
    private final IndexTypeVector<VertexIndex, CornerIndex> vertexCorners =
            new IndexTypeVector<>(CornerIndex.type());

    @Getter
    private int numOriginalVertices = 0, numDegeneratedFaces = 0, numIsolatedVertices = 0;
    private final IndexTypeVector<VertexIndex, VertexIndex> nonManifoldVertexParents =
            new IndexTypeVector<>(VertexIndex.type());

    @Getter
    private final ValenceCache<CornerTable> valenceCache = new ValenceCache<>(this);

    public static StatusOr<CornerTable> create(IndexTypeVector<FaceIndex, FaceType> faces) {
        StatusChain chain = new StatusChain();
        CornerTable ct = new CornerTable();
        return ct.init(faces).isError(chain) ? StatusOr.error(chain.get()) : StatusOr.ok(ct);
    }

    /**
     * Initializes the CornerTable from provided set of indexed faces.
     * The input faces can represent a non-manifold topology, in which case the
     * non-manifold edges and vertices are going to be split.
     */
    public Status init(IndexTypeVector<FaceIndex, FaceType> faces) {
        StatusChain chain = new StatusChain();

        valenceCache.clearValenceCache();
        valenceCache.clearValenceCacheInaccurate();
        cornerToVertexMap.resize(faces.size() * 3);
        for (FaceIndex fi : FaceIndex.range(0, (int) faces.size())) {
            for (int i = 0; i < 3; ++i) {
                cornerToVertexMap.set(this.getFirstCorner(fi).add(i), faces.get(fi).get(i));
            }
        }
        AtomicReference<Integer> numVertices = new AtomicReference<>(-1);
        if (this.computeOppositeCorners(numVertices).isError(chain)) return chain.get();
        if (this.breakNonManifoldEdges().isError(chain)) return chain.get();
        return this.computeVertexCorners(numVertices.get());
    }

    /** Resets the corner table to the given number of invalid faces. */
    public Status reset(int numFaces) {
        return this.reset(numFaces, numFaces * 3);
    }

    /** Resets the corner table to the given number of invalid faces and vertices. */
    public Status reset(int numFaces, int numVertices) {
        if (numFaces < 0 || numVertices < 0) {
            return Status.dracoError("Invalid number of faces or vertices");
        }
        if (numFaces > Integer.MAX_VALUE / 3) {
            return Status.dracoError("Number of faces is too large");
        }
        cornerToVertexMap.assign(numFaces * 3, VertexIndex.INVALID);
        oppositeCorners.assign(numFaces * 3, CornerIndex.INVALID);
        vertexCorners.reserve(numVertices);
        valenceCache.clearValenceCache();
        valenceCache.clearValenceCacheInaccurate();
        return Status.ok();
    }

    public int getNumVertices() { return (int) vertexCorners.size(); }
    public int getNumCorners() { return (int) cornerToVertexMap.size(); }
    public int getNumFaces() { return (int) cornerToVertexMap.size() / 3; }

    public CornerIndex opposite(CornerIndex corner) {
        if(corner.isInvalid()) return corner;
        return this.oppositeCorners.get(corner);
    }
    public CornerIndex next(CornerIndex corner) {
        if(corner.isInvalid()) return corner;
        corner = corner.add(1);
        return this.localIndex(corner) != 0 ? corner : corner.subtract(3);
    }
    public CornerIndex previous(CornerIndex corner) {
        if(corner.isInvalid()) return corner;
        return this.localIndex(corner) != 0 ? corner.subtract(1) : corner.add(2);
    }
    public VertexIndex getVertex(CornerIndex corner) {
        if(corner.isInvalid()) return VertexIndex.INVALID;
        return this.getConfidentVertex(corner);
    }
    public VertexIndex getConfidentVertex(CornerIndex corner) {
        if(corner.getValue() < 0 || this.getNumCorners() <= corner.getValue()) {
            return VertexIndex.INVALID;
        }
        return this.cornerToVertexMap.get(corner);
    }
    public FaceIndex getFace(CornerIndex corner) {
        if(corner.isInvalid()) return FaceIndex.INVALID;
        return FaceIndex.of(corner.getValue() / 3);
    }
    public CornerIndex getFirstCorner(FaceIndex face) {
        if(face.isInvalid()) return CornerIndex.INVALID;
        return CornerIndex.of(face.getValue() * 3);
    }
    public CornerIndex[] getAllCorners(FaceIndex face) {
        CornerIndex ci = CornerIndex.of(face.getValue() * 3);
        return new CornerIndex[] { ci, ci.add(1), ci.add(2) };
    }
    public int localIndex(CornerIndex corner) {
        return corner.getValue() % 3;
    }

    public FaceType getFaceData(FaceIndex face) {
        CornerIndex firstCorner = this.getFirstCorner(face);
        FaceType faceData = new FaceType();
        for(int i = 0; i < 3; i++) {
            faceData.set(i, this.cornerToVertexMap.get(firstCorner.add(i)));
        }
        return faceData;
    }

    public void setFaceData(FaceIndex face, FaceType data) {
        this.checkValenceCacheEmpty();
        CornerIndex firstCorner = this.getFirstCorner(face);
        for(int i = 0; i < 3; i++) {
            this.cornerToVertexMap.set(firstCorner.add(i), data.get(i));
        }
    }

    /**
     * Returns the left-most corner of the given vertex 1-ring. If a vertex is not
     * on a boundary (in which case it has a full 1-ring), this function returns
     * any of the corners mapped to the given vertex.
     */
    public CornerIndex getLeftMostCorner(VertexIndex vertex) {
        return this.vertexCorners.get(vertex);
    }

    /** Returns the parent vertex of the given corner table vertex. */
    public VertexIndex getVertexParent(VertexIndex vertex) {
        if(vertex.getValue() < this.numOriginalVertices) return vertex;
        VertexIndex index = vertex.subtract(this.numOriginalVertices);
        return this.nonManifoldVertexParents.get(index);
    }

    /** Returns true if the corner is valid. */
    public boolean isValid(CornerIndex c) {
        return this.getVertex(c).isValid();
    }

    public int getValence(VertexIndex v) {
        if(v.isInvalid()) return -1;
        return this.getConfidentValence(v);
    }

    public int getConfidentValence(VertexIndex v) {
        if(v.getValue() < 0 || v.getValue() >= this.getNumVertices()) {
            return -1;
        }
        VertexRingIterator<CornerTable> vi = new VertexRingIterator<>(this, v);
        int valence = 0;
        for(; vi.hasNext(); vi.next()) ++valence;
        return valence;
    }

    public int getValence(CornerIndex corner) {
        if(corner.isInvalid()) return -1;
        return this.getConfidentValence(corner);
    }

    public int getConfidentValence(CornerIndex c) {
        if(c.getValue() >= this.getNumCorners()) {
            throw new IllegalStateException("Invalid corner index");
        }
        return this.getConfidentValence(this.getConfidentVertex(c));
    }

    public boolean isOnBoundary(VertexIndex vert) {
        CornerIndex corner = this.getLeftMostCorner(vert);
        return this.swingLeft(corner).isInvalid();
    }

    public CornerIndex swingRight(CornerIndex corner) {
        return this.previous(this.opposite(this.previous(corner)));
    }
    public CornerIndex swingLeft(CornerIndex corner) {
        return this.next(this.opposite(this.next(corner)));
    }

    public CornerIndex getLeftCorner(CornerIndex cornerId) {
        if(cornerId.isInvalid()) return CornerIndex.INVALID;
        return this.opposite(this.previous(cornerId));
    }
    public CornerIndex getRightCorner(CornerIndex cornerId) {
        if(cornerId.isInvalid()) return CornerIndex.INVALID;
        return this.opposite(this.next(cornerId));
    }

    public int getNumNewVertices() {
        return this.getNumVertices() - this.numOriginalVertices;
    }

    public boolean isDegenerated(FaceIndex face) {
        if(face.isInvalid()) return true;
        CornerIndex firstFaceCorner = this.getFirstCorner(face);
        VertexIndex v0 = this.getVertex(firstFaceCorner);
        VertexIndex v1 = this.getVertex(this.next(firstFaceCorner));
        VertexIndex v2 = this.getVertex(this.previous(firstFaceCorner));
        return v0.equals(v1) || v0.equals(v2) || v1.equals(v2);
    }

    public void setOppositeCorner(CornerIndex cornerId, CornerIndex oppCornerId) {
        this.checkValenceCacheEmpty();
        this.oppositeCorners.set(cornerId, oppCornerId);
    }

    public void setOppositeCorners(CornerIndex corner0, CornerIndex corner1) {
        this.checkValenceCacheEmpty();
        if(corner0.isValid()) this.oppositeCorners.set(corner0, corner1);
        if(corner1.isValid()) this.oppositeCorners.set(corner1, corner0);
    }

    public void mapCornerToVertex(CornerIndex cornerId, VertexIndex vertId) {
        this.checkValenceCacheEmpty();
        this.cornerToVertexMap.set(cornerId, vertId);
    }

    public VertexIndex addNewVertex() {
        this.checkValenceCacheEmpty();
        vertexCorners.pushBack(CornerIndex.INVALID);
        return VertexIndex.of((int) (vertexCorners.size() - 1));
    }

    public FaceIndex addNewFace(VertexIndex[] vertices) {
        FaceIndex newFaceIndex = FaceIndex.of(this.getNumFaces());
        for(int i = 0; i < 3; ++i) {
            cornerToVertexMap.pushBack(vertices[i]);
            this.setLeftMostCorner(vertices[i], CornerIndex.of((int) (cornerToVertexMap.size() - 1)));
        }
        oppositeCorners.resize(cornerToVertexMap.size(), CornerIndex.INVALID);
        return newFaceIndex;
    }

    public void setLeftMostCorner(VertexIndex vert, CornerIndex corner) {
        this.checkValenceCacheEmpty();
        if(vert.isValid()) vertexCorners.set(vert, corner);
    }

    public void updateVertexToCornerMap(VertexIndex vert) {
        this.checkValenceCacheEmpty();
        CornerIndex firstC = vertexCorners.get(vert);
        if(firstC.isInvalid()) return;
        CornerIndex actC = this.swingLeft(firstC);
        CornerIndex c = firstC;
        while(actC.isValid() && !actC.equals(firstC)) {
            c = actC;
            actC = this.swingLeft(actC);
        }
        if(!actC.equals(firstC)) {
            vertexCorners.set(vert, c);
        }
    }

    public void setNumVertices(int numVertices) {
        this.checkValenceCacheEmpty();
        vertexCorners.resize(numVertices, CornerIndex.INVALID);
    }

    public void makeVertexIsolated(VertexIndex vert) {
        this.checkValenceCacheEmpty();
        vertexCorners.set(vert, CornerIndex.INVALID);
    }

    public boolean isVertexIsolated(VertexIndex v) {
        return this.getLeftMostCorner(v).isInvalid();
    }

    public void makeFaceInvalid(FaceIndex face) {
        this.checkValenceCacheEmpty();
        if(face.isInvalid()) return;
        CornerIndex firstCorner = this.getFirstCorner(face);
        for(int i = 0; i < 3; ++i) {
            this.cornerToVertexMap.set(firstCorner.add(i), VertexIndex.INVALID);
        }
    }

    public void updateFaceToVertexMap(VertexIndex vertex) {
        if(!this.getValenceCache().isCacheEmpty()) {
            throw new IllegalStateException("Valence cache is not empty");
        }
        for(CornerIndex corner : VertexCornersIterator.iterable(this, vertex)) {
            cornerToVertexMap.set(corner, vertex);
        }
    }

    // Create a storage for half-edges on each vertex. We store all half-edges in
    // one array, where each entry is identified by the half-edge's sink vertex id
    // and the associated half-edge corner id (corner opposite to the half-edge).
    // Each vertex will be assigned storage for up to
    // numCornersOnVertices[vert_id] half-edges. Unused half-edges are marked
    // with sink_vert == VertexIndex.INVALID.
    static class VertexEdgePair {
        VertexIndex sinkVert = VertexIndex.INVALID;
        CornerIndex edgeCorner = CornerIndex.INVALID;
    }

    private Status computeOppositeCorners(AtomicReference<Integer> numVertices) {
        this.checkValenceCacheEmpty();
        if(numVertices == null) {
            return Status.dracoError("numVertices is null");
        }
        oppositeCorners.resize(this.getNumCorners(), CornerIndex.INVALID);

        // Out implementation for finding opposite corners is based on keeping track
        // of outgoing half-edges for each vertex of the mesh. Half-edges (defined by
        // their opposite corners) are processed one by one and whenever a new
        // half-edge (corner) is processed, we check whether the sink vertex of
        // this half-edge contains its sibling half-edge. If yes, we connect them and
        // remove the sibling half-edge from the sink vertex, otherwise we add the new
        // half-edge to its source vertex.

        // First compute the number of outgoing half-edges (corners) attached to each
        // vertex.
        IndexTypeVector<VertexIndex, Integer> numCornersOnVertices =
                new IndexTypeVector<>(DataType.int32());
        numCornersOnVertices.reserve(this.getNumCorners());
        for (CornerIndex c : CornerIndex.range(0, this.getNumCorners())) {
            VertexIndex v1 = this.getVertex(c);
            if (v1.getValue() >= numCornersOnVertices.size()) {
                numCornersOnVertices.resize(v1.getValue() + 1, 0);
            }
            numCornersOnVertices.set(v1, numCornersOnVertices.get(v1) + 1);
        }

        CppVector<VertexEdgePair> vertexEdges = new CppVector<>(VertexEdgePair::new, this.getNumCorners());

        // For each vertex compute the offset (location where the first half-edge
        // entry of a given vertex is going to be stored). This way each vertex is
        // guaranteed to have a non-overlapping storage with respect to the other
        // vertices.
        IndexTypeVector<VertexIndex, Integer> vertexOffset =
                new IndexTypeVector<>(DataType.int32(), numCornersOnVertices.size());
        int offset = 0;
        for (VertexIndex i : VertexIndex.range(0, (int) numCornersOnVertices.size())) {
            vertexOffset.set(i, offset);
            offset += numCornersOnVertices.get(i);
        }

        // Now go over the all half-edges (using their opposite corners) and either
        // insert them to the vertex_edge array or connect them with existing
        // half-edges.
        for(CornerIndex c = CornerIndex.of(0); c.getValue() < this.getNumCorners(); c = c.increment()) {
            VertexIndex tipV = this.getVertex(c);
            VertexIndex sourceV = this.getVertex(this.next(c));
            VertexIndex sinkV = this.getVertex(this.previous(c));

            FaceIndex faceIndex = this.getFace(c);
            if(c.equals(this.getFirstCorner(faceIndex))) {
                // Check whether the face is degenerated, if so ignore it.
                VertexIndex v0 = this.getVertex(c);
                if(v0.equals(sourceV) || v0.equals(sinkV) || sourceV.equals(sinkV)) {
                    ++numDegeneratedFaces;
                    c = c.add(2);  // Ignore the next two corners of the same face.
                    continue;
                }
            }

            CornerIndex oppositeC = CornerIndex.INVALID;
            // The maximum number of half-edges attached to the sink vertex.
            int numCornersOnVert = numCornersOnVertices.get(sinkV);
            // Where to look for the first half-edge on the sink vertex.
            offset = vertexOffset.get(sinkV);
            for(int i = 0; i < numCornersOnVert; ++i, ++offset) {
                VertexIndex otherV = vertexEdges.get(offset).sinkVert;
                if(otherV.isInvalid()) break; // No matching half-edge found on the sink vertex.

                if(otherV.equals(sourceV)) {
                    if (tipV.equals(this.getVertex(vertexEdges.get(offset).edgeCorner))) {
                        continue;  // Don't connect mirrored faces.
                    }
                    // A matching half-edge was found on the sink vertex. Mark the
                    // half-edge's opposite corner.
                    oppositeC = vertexEdges.get(offset).edgeCorner;
                    // Remove the half-edge from the sink vertex. We remap all subsequent
                    // half-edges one slot down.
                    for(int j = i + 1; j < numCornersOnVert; ++j, ++offset) {
                        vertexEdges.set(offset, vertexEdges.get(offset + 1));
                        if(vertexEdges.get(offset).sinkVert.isInvalid()) break; // Unused half-edge reached.
                    }
                    // Mark the last entry as unused.
                    vertexEdges.get(offset).sinkVert = VertexIndex.INVALID;
                    break;
                }
            }
            if(oppositeC.isInvalid()) {
                // No opposite corner have been found. Insert the new edge
                int numCornersOnSourceVert = numCornersOnVertices.get(sourceV);
                offset = vertexOffset.get(sourceV);
                for(int i = 0; i < numCornersOnSourceVert; ++i, ++offset) {
                    // Find the first unused half-edge slot on the source vertex.
                    if(vertexEdges.get(offset).sinkVert.isInvalid()) {
                        vertexEdges.get(offset).sinkVert = sinkV;
                        vertexEdges.get(offset).edgeCorner = c;
                        break;
                    }
                }
            } else {
                // Opposite corner found.
                oppositeCorners.set(c, oppositeC);
                oppositeCorners.set(oppositeC, c);
            }
        }
        numVertices.set((int) numCornersOnVertices.size());
        return Status.ok();
    }

    private Status breakNonManifoldEdges() {
        // This function detects and breaks non-manifold edges that are caused by
        // folds in 1-ring neighborhood around a vertex. Non-manifold edges can occur
        // when the 1-ring surface around a vertex self-intersects in a common edge.
        // For example imagine a surface around a pivot vertex 0, where the 1-ring
        // is defined by vertices |1, 2, 3, 1, 4|. The surface passes edge <0, 1>
        // twice which would result in a non-manifold edge that needs to be broken.
        // For now all faces connected to these non-manifold edges are disconnected
        // resulting in open boundaries on the mesh. New vertices will be created
        // automatically for each new disjoint patch in the ComputeVertexCorners()
        // method.
        // Note that all other non-manifold edges are implicitly handled by the
        // function ComputeVertexCorners() that automatically creates new vertices
        // on disjoint 1-ring surface patches.

        IndexTypeVector<CornerIndex, Boolean> visitedCorners =
                new IndexTypeVector<>(DataType.bool(), this.getNumCorners(), false);
        List<Pair<VertexIndex, CornerIndex>> sinkVertices = new ArrayList<>();
        boolean meshConnectivityUpdated;
        do {
            meshConnectivityUpdated = false;
            for(CornerIndex c : CornerIndex.range(0, this.getNumCorners())) {
                if(visitedCorners.get(c)) continue;
                sinkVertices.clear();

                // First swing all the way to find the left-most corner connected to the
                // corner's vertex.
                CornerIndex firstC = c;
                CornerIndex currentC = c;
                CornerIndex nextC;
                while(!(nextC = this.swingLeft(currentC)).equals(firstC) && nextC.isValid() && !visitedCorners.get(nextC)) {
                    currentC = nextC;
                }

                firstC = currentC;

                // Swing right from the first corner and check if all visited edges
                // are unique.
                do {
                    visitedCorners.set(currentC, true);
                    // Each new edge is defined by the pivot vertex (that is the same for
                    // all faces) and by the sink vertex (that is the |next| vertex from the
                    // currently processed pivot corner. I.e., each edge is uniquely defined
                    // by the sink vertex index.
                    CornerIndex sinkC = this.next(currentC);
                    VertexIndex sinkV = this.cornerToVertexMap.get(sinkC);

                    // Corner that defines the edge on the face.
                    CornerIndex edgeCorner = this.previous(currentC);
                    boolean vertexConnectivityUpdated = false;
                    // Go over all processed edges (sink vertices). If the current sink
                    // vertex has been already encountered before it may indicate a
                    // non-manifold edge that needs to be broken.
                    for(Pair<VertexIndex, CornerIndex> attachedSinkVertex : sinkVertices) {
                        if(attachedSinkVertex.getLeft().equals(sinkV)) {
                            // Sink vertex has been already processed.
                            CornerIndex otherEdgeCorner = attachedSinkVertex.getRight();
                            CornerIndex oppEdgeCorner = this.opposite(edgeCorner);

                            if(oppEdgeCorner.equals(otherEdgeCorner)) {
                                // We are closing the loop so no need to change the connectivity.
                                continue;
                            }

                            // Break the connectivity on the non-manifold edge.
                            CornerIndex oppOtherEdgeCorner = this.opposite(otherEdgeCorner);
                            if(oppEdgeCorner.isValid()) this.setOppositeCorner(oppEdgeCorner, CornerIndex.INVALID);
                            if(oppOtherEdgeCorner.isValid()) this.setOppositeCorner(oppOtherEdgeCorner, CornerIndex.INVALID);

                            this.setOppositeCorner(edgeCorner, CornerIndex.INVALID);
                            this.setOppositeCorner(otherEdgeCorner, CornerIndex.INVALID);

                            vertexConnectivityUpdated = true;
                            break;
                        }
                    }
                    if(vertexConnectivityUpdated) {
                        // Because of the updated connectivity, not all corners connected to
                        // this vertex have been processed and we need to go over them again.
                        // This can be optimized as we don't really need to
                        // iterate over all corners.
                        meshConnectivityUpdated = true;
                        break;
                    }
                    // Insert new sink vertex information <sink vertex index, edge corner>.
                    Pair<VertexIndex, CornerIndex> newSinkVert = Pair.of(
                            this.cornerToVertexMap.get(this.previous(currentC)), sinkC
                    );
                    sinkVertices.add(newSinkVert);

                    currentC = this.swingRight(currentC);
                } while(!currentC.equals(firstC) && currentC.isValid());
            }
        } while(meshConnectivityUpdated);

        return Status.ok();
    }

    private Status computeVertexCorners(int numVertices) {
        this.checkValenceCacheEmpty();
        numOriginalVertices = numVertices;
        vertexCorners.resize(numVertices, CornerIndex.INVALID);
        // Arrays for marking visited vertices and corners that allow us to detect
        // non-manifold vertices.
        IndexTypeVector<VertexIndex, Boolean> visitedVertices =
                new IndexTypeVector<>(DataType.bool(), numVertices, false);
        IndexTypeVector<CornerIndex, Boolean> visitedCorners =
                new IndexTypeVector<>(DataType.bool(), this.getNumCorners(), false);

        for(FaceIndex f : FaceIndex.range(0, this.getNumFaces())) {
            CornerIndex firstFaceCorner = this.getFirstCorner(f);
            // Check whether the face is degenerated. If so ignore it.
            if(this.isDegenerated(f)) continue;

            for(int k = 0; k < 3; ++k) {
                CornerIndex c = firstFaceCorner.add(k);
                if(visitedCorners.get(c)) continue;
                VertexIndex v = this.cornerToVertexMap.get(c);
                // Note that one vertex maps to many corners, but we just keep track
                // of the vertex which has a boundary on the left if the vertex lies on
                // the boundary. This means that all the related corners can be accessed
                // by iterating over the SwingRight() operator.
                // In case of a vertex inside the mesh, the choice is arbitrary.
                boolean isNonManifoldVertex = false;
                if(visitedVertices.get(v)) {
                    // A visited vertex of an unvisited corner found. Must be a non-manifold
                    // vertex.
                    // Create a new vertex for it.
                    vertexCorners.pushBack(CornerIndex.INVALID);
                    nonManifoldVertexParents.pushBack(v);
                    visitedVertices.pushBack(false);
                    v = VertexIndex.of(numVertices++);
                    isNonManifoldVertex = true;
                }
                // Mark the vertex as visited.
                visitedVertices.set(v, true);

                // First swing all the way to the left and mark all corners on the way.
                CornerIndex actC = c;
                while(actC.isValid()) {
                    visitedCorners.set(actC, true);
                    // Vertex will eventually point to the left most corner.
                    vertexCorners.set(v, actC);
                    if(isNonManifoldVertex) {
                        // Update vertex index in the corresponding face.
                        cornerToVertexMap.set(actC, v);
                    }
                    actC = this.swingLeft(actC);
                    if(actC.equals(c)) {
                        break;  // Full circle reached.
                    }
                }
                if(actC.isInvalid()) {
                    // If we have reached an open boundary we need to swing right from the
                    // initial corner to mark all corners in the opposite direction.
                    actC = this.swingRight(c);
                    while(actC.isValid()) {
                        visitedCorners.set(actC, true);
                        if(isNonManifoldVertex) {
                            // Update vertex index in the corresponding face.
                            cornerToVertexMap.set(actC, v);
                        }
                        actC = this.swingRight(actC);
                    }
                }
            }
        }

        // Count the number of isolated (unprocessed) vertices.
        this.numIsolatedVertices = 0;
        for(boolean visited : visitedVertices) {
            if(!visited) ++this.numIsolatedVertices;
        }
        return Status.ok();
    }

    private void checkValenceCacheEmpty() {
        if(!this.getValenceCache().isCacheEmpty()) {
            throw new IllegalStateException("Valence cache is not empty");
        }
    }
}
