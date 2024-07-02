package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

import java.util.Iterator;

public class VertexCornersIterator<T extends ICornerTable> implements Iterator<CornerIndex> {

    private final T cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;
    private boolean leftTraversal, started = false;

    public VertexCornersIterator() {
        this.cornerTable = null;
        this.startCorner = CornerIndex.INVALID;
        this.corner = startCorner;
        this.leftTraversal = true;
    }

    public VertexCornersIterator(T cornerTable, VertexIndex vertexIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerTable.getLeftMostCorner(vertexIndex);
        this.corner = startCorner;
        this.leftTraversal = true;
    }

    public VertexCornersIterator(T cornerTable, CornerIndex cornerIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerIndex;
        this.corner = startCorner;
        this.leftTraversal = true;
    }

    @Override
    public boolean hasNext() {
        return corner != CornerIndex.INVALID;
    }

    @Override
    public CornerIndex next() {
        if(!started) {
            started = true;
            return startCorner;
        }
        if(leftTraversal) {
            corner = cornerTable.swingLeft(corner);
            if(corner == CornerIndex.INVALID) {
                corner = cornerTable.swingRight(startCorner);
                leftTraversal = false;
            } else if(corner.equals(startCorner)) {
                corner = CornerIndex.INVALID;
            }
        } else {
            corner = cornerTable.swingRight(corner);
        }
        return corner;
    }

    public static <T extends ICornerTable> VertexCornersIterator<T> endIterator(VertexCornersIterator<T> other) {
        VertexCornersIterator<T> ret = new VertexCornersIterator<>();
        ret.corner = CornerIndex.INVALID;
        return ret;
    }
}
