package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;

public interface McCoordTransformer {
    McCoord transform(McCoord coord);
}
