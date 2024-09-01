package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.McCoordTransformer;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GraphicsModel {
    private final NativeTextureWrapper textureObject;
    private final GraphicsShapes shapes;

    public void drawAndRender(DrawContextWrapper<?> drawContextWrapper,
                              McCoordTransformer transformer, float alpha) {
        McConnector.client().glGraphicsManager.setShaderTexture(textureObject);
        shapes.drawAndRender(drawContextWrapper, transformer, alpha);
    }
}
