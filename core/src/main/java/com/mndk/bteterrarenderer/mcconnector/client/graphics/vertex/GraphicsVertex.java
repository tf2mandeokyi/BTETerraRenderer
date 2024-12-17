package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

public abstract class GraphicsVertex<T extends GraphicsVertex<T>> {
    public abstract T transformMcCoord(McCoordTransformer transformer);
}