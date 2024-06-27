package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;

import java.util.Iterator;

public class FaceAdjacencyIterator implements Iterator<FaceIndex> {

    private final CornerTable cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;

    public FaceAdjacencyIterator() {
        this.cornerTable = null;
        this.startCorner = CornerIndex.INVALID;
        this.corner = startCorner;
    }

    public FaceAdjacencyIterator(CornerTable cornerTable, FaceIndex faceIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerTable.getFirstCorner(faceIndex);
        this.corner = startCorner;
        if(cornerTable.opposite(corner) == CornerIndex.INVALID) {
            findNextFaceNeighbor();
        }
    }

    @Override
    public boolean hasNext() {
        return corner != CornerIndex.INVALID;
    }

    @Override
    public FaceIndex next() {
        FaceIndex face = cornerTable.getFace(cornerTable.opposite(corner));
        findNextFaceNeighbor();
        return face;
    }

    private void findNextFaceNeighbor() {
        while(corner != CornerIndex.INVALID) {
            corner = cornerTable.next(corner);
            if(corner.equals(startCorner)) {
                corner = CornerIndex.INVALID;
                return;
            }
            if(cornerTable.opposite(corner) != CornerIndex.INVALID) {
                return;
            }
        }
    }

    public static FaceAdjacencyIterator endIterator(FaceAdjacencyIterator other) {
        FaceAdjacencyIterator ret = new FaceAdjacencyIterator();
        ret.corner = CornerIndex.INVALID;
        return ret;
    }
}
