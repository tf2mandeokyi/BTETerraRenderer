package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

import java.util.Iterator;

public class VertexCornersIterator implements Iterator<CornerIndex> {

    public static Iterable<CornerIndex> iterable(ICornerTable cornerTable, VertexIndex vertexIndex) {
        return () -> new VertexCornersIterator(cornerTable, vertexIndex);
    }
    public static Iterable<CornerIndex> iterable(ICornerTable cornerTable, CornerIndex cornerIndex) {
        return () -> new VertexCornersIterator(cornerTable, cornerIndex);
    }

    private final ICornerTable cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;
    private boolean leftTraversal;

    private VertexCornersIterator(ICornerTable table, VertexIndex vertexIndex) {
        this.cornerTable = table;
        this.startCorner = table.getLeftMostCorner(vertexIndex);
        this.corner = null;
        this.leftTraversal = true;
    }

    private VertexCornersIterator(ICornerTable table, CornerIndex cornerIndex) {
        this.cornerTable = table;
        this.startCorner = cornerIndex;
        this.corner = null;
        this.leftTraversal = true;
    }

    @Override public boolean hasNext() { return getNext(false).isValid(); }
    @Override public CornerIndex next() { return getNext(true); }

    private CornerIndex getNext(boolean apply) {
        CornerIndex tempCorner = corner;
        boolean tempTraversal = leftTraversal;

        if(tempCorner == null) {
            tempCorner = startCorner;
        } else if(tempTraversal) {
            tempCorner = cornerTable.swingLeft(tempCorner);
            if(tempCorner.isInvalid()) {
                tempCorner = cornerTable.swingRight(startCorner);
                tempTraversal = false;
            } else if(tempCorner.equals(startCorner)) {
                tempCorner = CornerIndex.INVALID;
            }
        } else {
            tempCorner = cornerTable.swingRight(tempCorner);
        }

        if(apply) {
            corner = tempCorner;
            leftTraversal = tempTraversal;
        }
        return tempCorner;
    }
}
