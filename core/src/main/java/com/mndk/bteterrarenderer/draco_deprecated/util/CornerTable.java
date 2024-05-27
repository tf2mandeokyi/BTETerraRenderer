package com.mndk.bteterrarenderer.draco_deprecated.util;

import com.mndk.bteterrarenderer.draco.core.DracoVector;

public class CornerTable {

    public static final int kInvalidCornerIndex = -1;

    private final DracoVector<Integer> opposite_corners_ = new DracoVector<>(kInvalidCornerIndex);

    /**
     * @implNote Draco 24.3: <a href="https://google.github.io/draco/spec/#posopposite">
     *     PosOpposite</a>
     */
    public int opposite(int corner) {
        return opposite_corners_.get(corner);
    }

    /**
     * @implNote Draco 24.13: <a href="https://google.github.io/draco/spec/#setoppositecorners">
     *     SetOppositeCorners</a>
     */
    public void setOpposite(int corner, int oppositeCorner) {
        opposite_corners_.set(corner, oppositeCorner);
        opposite_corners_.set(oppositeCorner, corner);
        System.out.println("      " + opposite_corners_);
    }

    /**
     * @implNote Draco 24.1: <a href="https://google.github.io/draco/spec/#next">
     *     Next</a>
     */
    public static int next(int corner) {
        if(corner == kInvalidCornerIndex) return corner;
        return ((corner % 3) == 2) ? corner - 2 : corner + 1;
    }

    /**
     * @implNote Draco 24.2: <a href="https://google.github.io/draco/spec/#previous">
     *     Previous</a>
     */
    public static int previous(int corner) {
        if(corner == kInvalidCornerIndex) return corner;
        return ((corner % 3) == 0) ? corner + 2 : corner - 1;
    }
}
