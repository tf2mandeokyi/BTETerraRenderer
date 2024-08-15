package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

import java.util.Iterator;

public class VertexRingIterator<T extends ICornerTable> implements Iterator<VertexIndex> {

    private final T cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;
    private boolean leftTraversal;

    public VertexRingIterator(T cornerTable, VertexIndex vertexIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerTable.getLeftMostCorner(vertexIndex);
        this.corner = null;
        this.leftTraversal = true;
    }

    @Override public boolean hasNext() { return getNext(false).isValid(); }
    @Override public VertexIndex next() { return getNext(true); }

    private VertexIndex getNext(boolean apply) {
        CornerIndex tempCorner = corner;
        boolean tempTraversal = leftTraversal;

        if(tempCorner == null) {
            tempCorner = startCorner;
        } else if(tempTraversal) {
            tempCorner = cornerTable.swingLeft(tempCorner);
            if (tempCorner.isInvalid()) {
                tempCorner = startCorner;
                tempTraversal = false;
            } else if (tempCorner.equals(startCorner)) {
                tempCorner = CornerIndex.INVALID;
            }
        } else {
            tempCorner = cornerTable.swingRight(tempCorner);
        }

        CornerIndex ringCorner = tempTraversal ? cornerTable.previous(tempCorner) : cornerTable.next(tempCorner);
        if(apply) {
            corner = tempCorner;
            leftTraversal = tempTraversal;
        }
        return cornerTable.getVertex(ringCorner);
    }
}
