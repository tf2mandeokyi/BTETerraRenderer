package com.mndk.bteterrarenderer.mcconnector.util.math;

public interface McCoordTransformer {
    McCoordTransformer IDENTITY = coord -> coord;

    McCoord transform(McCoord coord);
}
