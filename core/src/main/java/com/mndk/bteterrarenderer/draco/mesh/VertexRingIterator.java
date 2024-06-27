package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

import java.util.Iterator;

public class VertexRingIterator<T extends ICornerTable> implements Iterator<VertexIndex> {

    private final T cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;
    private boolean leftTraversal;

    public VertexRingIterator() {
        this.cornerTable = null;
        this.startCorner = CornerIndex.INVALID;
        this.corner = startCorner;
        this.leftTraversal = true;
    }

    public VertexRingIterator(T cornerTable, VertexIndex vertexIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerTable.getLeftMostCorner(vertexIndex);
        this.corner = startCorner;
        this.leftTraversal = true;
    }

    @Override
    public boolean hasNext() {
        return corner != CornerIndex.INVALID;
    }

    @Override
    public VertexIndex next() {
        if(leftTraversal) {
            corner = cornerTable.swingLeft(corner);
            if(corner == CornerIndex.INVALID) {
                corner = startCorner;
                leftTraversal = false;
            } else if(corner.equals(startCorner)) {
                corner = CornerIndex.INVALID;
            }
        } else {
            corner = cornerTable.swingRight(corner);
        }

        CornerIndex ringCorner = leftTraversal ? cornerTable.previous(corner) : cornerTable.next(corner);
        return cornerTable.getVertex(ringCorner);
    }

    public static <T extends ICornerTable> VertexRingIterator<T> endIterator(VertexRingIterator<T> other) {
        VertexRingIterator<T> ret = new VertexRingIterator<>();
        ret.corner = CornerIndex.INVALID;
        return ret;
    }
}
