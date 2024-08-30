/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.Getter;

public class MeshAttributeCornerTable implements ICornerTable {

    private final IndexTypeVector<CornerIndex, Boolean> isEdgeOnSeam =
            new IndexTypeVector<>(DataType.bool());
    private final IndexTypeVector<VertexIndex, Boolean> isVertexOnSeam =
            new IndexTypeVector<>(DataType.bool());

    /**
     * If this is set to true, it means that there are no attribute seams between
     * two faces. This can be used to speed up some algorithms.
     */
    @Getter
    private boolean noInteriorSeams = true;

    private final IndexTypeVector<CornerIndex, VertexIndex> cornerToVertexMap =
            new IndexTypeVector<>(VertexIndex.type());

    /**
     * Map between vertices and their associated left most corners. A left most
     * corner is a corner that is adjacent to a boundary or an attribute seam from
     * right (i.e., SwingLeft from that corner will return an invalid corner). If
     * no such corner exists for a given vertex, then any corner attached to the
     * vertex can be used.
     */
    private final IndexTypeVector<VertexIndex, CornerIndex> vertexToLeftMostCornerMap =
            new IndexTypeVector<>(CornerIndex.type());

    /**
     * Map between vertex ids and attribute entry ids (i.e. the values stored in
     * the attribute buffer). The attribute entry id can be retrieved using the
     * VertexParent() method.
     */
    private final IndexTypeVector<VertexIndex, AttributeValueIndex> vertexToAttributeEntryIdMap =
            new IndexTypeVector<>(AttributeValueIndex.type());

    @Getter
    private CornerTable cornerTable = null;
    @Getter
    private final ValenceCache<MeshAttributeCornerTable> valenceCache = new ValenceCache<>(this);

    public MeshAttributeCornerTable() {}

    public Status initEmpty(CornerTable table) {
        if (table == null) {
            return Status.invalidParameter("Table is null");
        }
        valenceCache.clearValenceCache();
        valenceCache.clearValenceCacheInaccurate();
        isEdgeOnSeam.assign(table.getNumCorners(), false);
        isVertexOnSeam.assign(table.getNumVertices(), false);
        cornerToVertexMap.assign(table.getNumCorners(), VertexIndex.INVALID);
        vertexToAttributeEntryIdMap.reserve(table.getNumVertices());
        vertexToLeftMostCornerMap.reserve(table.getNumVertices());
        cornerTable = table;
        noInteriorSeams = true;
        return Status.ok();
    }

    public Status initFromAttribute(Mesh mesh, CornerTable table, PointAttribute att) {
        StatusChain chain = new StatusChain();

        if(initEmpty(table).isError(chain)) return chain.get();
        valenceCache.clearValenceCache();
        valenceCache.clearValenceCacheInaccurate();

        // Find all necessary data for encoding attributes. For now, we check which of
        // the mesh vertices is part of an attribute seam, because seams require
        // special handling.
        for(CornerIndex c : CornerIndex.range(0, cornerTable.getNumCorners())) {
            FaceIndex f = cornerTable.getFace(c);
            if(cornerTable.isDegenerated(f)) continue; // Ignore corners on degenerated faces.

            CornerIndex oppCorner = cornerTable.opposite(c);
            if(oppCorner.isInvalid()) {
                // Boundary. Mark it as seam edge.
                isEdgeOnSeam.set(c, true);
                // Mark seam vertices.
                VertexIndex v = cornerTable.getVertex(cornerTable.next(c));
                isVertexOnSeam.set(v, true);
                v = cornerTable.getVertex(cornerTable.previous(c));
                isVertexOnSeam.set(v, true);
                continue;
            }
            if(oppCorner.getValue() < c.getValue()) continue; // Opposite corner was already processed.

            CornerIndex actC = c, actSiblingC = oppCorner;
            for(int i = 0; i < 2; ++i) {
                // Get the sibling corners. I.e., the two corners attached to the same
                // vertex but divided by the seam edge.
                actC = cornerTable.next(actC);
                actSiblingC = cornerTable.previous(actSiblingC);
                PointIndex pointId = mesh.cornerToPointId(actC.getValue());
                PointIndex siblingPointId = mesh.cornerToPointId(actSiblingC.getValue());
                if(!att.getMappedIndex(pointId).equals(att.getMappedIndex(siblingPointId))) {
                    noInteriorSeams = false;
                    isEdgeOnSeam.set(c, true);
                    isEdgeOnSeam.set(oppCorner, true);
                    // Mark seam vertices.
                    isVertexOnSeam.set(cornerTable.getVertex(cornerTable.next(c)), true);
                    isVertexOnSeam.set(cornerTable.getVertex(cornerTable.previous(c)), true);
                    isVertexOnSeam.set(cornerTable.getVertex(cornerTable.next(oppCorner)), true);
                    isVertexOnSeam.set(cornerTable.getVertex(cornerTable.previous(oppCorner)), true);
                    break;
                }
            }
        }

        return this.recomputeVertices(mesh, att);
    }

    public void addSeamEdge(CornerIndex c) {
        this.checkValenceCacheEmpty();
        isEdgeOnSeam.set(c, true);
        // Mark seam vertices.
        isVertexOnSeam.set(cornerTable.getVertex(cornerTable.next(c)), true);
        isVertexOnSeam.set(cornerTable.getVertex(cornerTable.previous(c)), true);

        CornerIndex oppCorner = cornerTable.opposite(c);
        if(oppCorner.isValid()) {
            noInteriorSeams = false;
            isEdgeOnSeam.set(oppCorner, true);
            isVertexOnSeam.set(cornerTable.getVertex(cornerTable.next(oppCorner)), true);
            isVertexOnSeam.set(cornerTable.getVertex(cornerTable.previous(oppCorner)), true);
        }
    }

    public Status recomputeVertices(Mesh mesh, PointAttribute att) {
        this.checkValenceCacheEmpty();
        if(mesh != null && att != null) {
            return this.recomputeVerticesInternal(true, mesh, att);
        } else {
            return this.recomputeVerticesInternal(false, null, null);
        }
    }

    private Status recomputeVerticesInternal(boolean initVertexToAttributeEntryMap, Mesh mesh, PointAttribute att) {
        this.checkValenceCacheEmpty();
        vertexToAttributeEntryIdMap.clear();
        vertexToLeftMostCornerMap.clear();
        int numNewVertices = 0;
        for(VertexIndex v : VertexIndex.range(0, cornerTable.getNumVertices())) {
            CornerIndex c = cornerTable.getLeftMostCorner(v);
            if(c.isInvalid()) continue; // Isolated vertex?

            AttributeValueIndex firstVertId = AttributeValueIndex.of(numNewVertices++);
            if(initVertexToAttributeEntryMap) {
                PointIndex pointId = mesh.cornerToPointId(c.getValue());
                vertexToAttributeEntryIdMap.pushBack(att.getMappedIndex(pointId));
            } else {
                // Identity mapping
                vertexToAttributeEntryIdMap.pushBack(firstVertId);
            }
            CornerIndex firstC = c;
            CornerIndex actC;
            // Check if the vertex is on a seam edge, if it is we need to find the first
            // attribute entry on the seam edge when traversing in the CCW direction.
            if(isVertexOnSeam.get(v)) {
                // Try to swing left on the modified corner table. We need to get the
                // first corner that defines an attribute seam.
                actC = this.swingLeft(firstC);
                while(actC.isValid()) {
                    firstC = actC;
                    actC = this.swingLeft(actC);
                    if(actC.equals(c)) {
                        // We reached the initial corner which shouldn't happen when we swing
                        // left from |c|.
                        return Status.dracoError("Unexpected corner reached");
                    }
                }
            }
            cornerToVertexMap.set(firstC, VertexIndex.of(firstVertId.getValue()));
            vertexToLeftMostCornerMap.pushBack(firstC);
            actC = cornerTable.swingRight(firstC);
            while(actC.isValid() && !actC.equals(firstC)) {
                if(this.isCornerOppositeToSeamEdge(cornerTable.next(actC))) {
                    firstVertId = AttributeValueIndex.of(numNewVertices++);
                    if(initVertexToAttributeEntryMap) {
                        PointIndex pointId = mesh.cornerToPointId(actC.getValue());
                        vertexToAttributeEntryIdMap.pushBack(att.getMappedIndex(pointId));
                    } else {
                        // Identity mapping.
                        vertexToAttributeEntryIdMap.pushBack(firstVertId);
                    }
                    vertexToLeftMostCornerMap.pushBack(actC);
                }
                cornerToVertexMap.set(actC, VertexIndex.of(firstVertId.getValue()));
                actC = cornerTable.swingRight(actC);
            }
        }
        return Status.ok();
    }

    public boolean isCornerOppositeToSeamEdge(CornerIndex corner) {
        return isEdgeOnSeam.get(corner);
    }

    public CornerIndex opposite(CornerIndex corner) {
        if(corner.isInvalid() || this.isCornerOppositeToSeamEdge(corner)) return CornerIndex.INVALID;
        return cornerTable.opposite(corner);
    }

    public CornerIndex next(CornerIndex corner) { return cornerTable.next(corner); }
    public CornerIndex previous(CornerIndex corner) { return cornerTable.previous(corner); }

    public boolean isCornerOnSeam(CornerIndex corner) {
        return isVertexOnSeam.get(cornerTable.getVertex(corner));
    }

    /** Similar to {@link CornerTable#getLeftCorner}, but does not go over seam edges. */
    public CornerIndex getLeftCorner(CornerIndex corner) {
        return this.opposite(this.previous(corner));
    }
    /** Similar to {@link CornerTable#getRightCorner}, but does not go over seam edges. */
    public CornerIndex getRightCorner(CornerIndex corner) {
        return this.opposite(this.next(corner));
    }

    /** Similar to {@link CornerTable#swingRight}, but it does not go over seam edges. */
    public CornerIndex swingRight(CornerIndex corner) {
        return this.previous(this.opposite(this.previous(corner)));
    }

    /** Similar to {@link CornerTable#swingLeft}, but it does not go over seam edges. */
    public CornerIndex swingLeft(CornerIndex corner) {
        return this.next(this.opposite(this.next(corner)));
    }

    public int getNumVertices() { return (int) vertexToAttributeEntryIdMap.size(); }
    public int getNumFaces() { return cornerTable.getNumFaces(); }
    public int getNumCorners() { return cornerTable.getNumCorners(); }

    public VertexIndex getVertex(CornerIndex corner) {
        if(cornerToVertexMap.size() <= corner.getValue()) {
            throw new IllegalArgumentException("Invalid corner index");
        }
        return this.getConfidentVertex(corner);
    }
    public VertexIndex getConfidentVertex(CornerIndex corner) {
        return cornerToVertexMap.get(corner);
    }
    /** Returns the attribute entry id associated to the given vertex. */
    public VertexIndex getVertexParent(VertexIndex vertex) {
        return VertexIndex.of(vertexToAttributeEntryIdMap.get(vertex).getValue());
    }

    public CornerIndex getLeftMostCorner(VertexIndex v) {
        return vertexToLeftMostCornerMap.get(v);
    }

    public FaceIndex getFace(CornerIndex corner) {
        return cornerTable.getFace(corner);
    }

    public CornerIndex getFirstCorner(FaceIndex face) {
        return cornerTable.getFirstCorner(face);
    }

    public CornerIndex[] getAllCorners(FaceIndex face) {
        return cornerTable.getAllCorners(face);
    }

    public boolean isOnBoundary(VertexIndex vert) {
        CornerIndex corner = this.getLeftMostCorner(vert);
        if(corner.isInvalid()) return true;
        return this.swingLeft(corner).isInvalid();
    }

    public boolean isDegenerated(FaceIndex face) {
        // Introducing seams can't change the degeneracy of the individual faces,
        // therefore we can delegate the check to the original cornerTable.
        return cornerTable.isDegenerated(face);
    }

    /**
     * Returns the valence (or degree) of a vertex.
     * Returns -1 if the given vertex index is not valid.
     */
    public int getValence(VertexIndex v) {
        if(v.isInvalid()) return -1;
        return this.getConfidentValence(v);
    }
    /** Same as {@link #getValence} but does not check for validity and does not return -1 */
    public int getConfidentValence(VertexIndex v) {
        if(v.getValue() >= this.getNumVertices()) {
            throw new IllegalArgumentException("Vertex index out of bounds");
        }
        VertexRingIterator<MeshAttributeCornerTable> vi = new VertexRingIterator<>(this, v);
        int valence = 0;
        for(; vi.hasNext(); vi.next()) valence++;
        return valence;
    }
    /** Returns the valence of the vertex at the given corner. */
    public int getValence(CornerIndex corner) {
        if(corner.getValue() >= this.getNumCorners()) {
            throw new IllegalArgumentException("Corner index out of bounds");
        }
        if(corner.isInvalid()) return -1;
        return this.getConfidentValence(corner);
    }
    public int getConfidentValence(CornerIndex c) {
        if(c.getValue() >= this.getNumCorners()) {
            throw new IllegalArgumentException("Corner index out of bounds");
        }
        return this.getConfidentValence(this.getVertex(c));
    }


    private void checkValenceCacheEmpty() {
        if(!this.getValenceCache().isCacheEmpty()) {
            throw new IllegalStateException("Valence cache is not empty");
        }
    }
}
