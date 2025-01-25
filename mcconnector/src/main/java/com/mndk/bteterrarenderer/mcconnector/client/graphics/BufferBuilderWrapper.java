package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

public interface BufferBuilderWrapper<S extends GraphicsShape> {
    void setContext(WorldDrawContextWrapper context);
    void setTransformer(McCoordTransformer modelPosTransformer);
    void nextShape(S shape);
    void drawAndRender();
}
