package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

public interface BufferBuilderWrapper<S extends GraphicsShape> {
    void nextShape(WorldDrawContextWrapper context, S shape, McCoordTransformer modelPosTransformer, float alpha);
    void drawAndRender(WorldDrawContextWrapper context);
}
