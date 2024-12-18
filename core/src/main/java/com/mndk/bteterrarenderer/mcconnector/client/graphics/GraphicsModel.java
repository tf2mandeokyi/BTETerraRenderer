package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GraphicsModel {
    private final NativeTextureWrapper textureObject;
    private final GraphicsShapes shapes;

    public void drawAndRender(DrawContextWrapper<?> drawContextWrapper,
                              McCoordTransformer modelPosTransformer, float alpha) {
        NativeTextureWrapper texture = this.textureObject.isDeleted()
                ? McConnector.client().glGraphicsManager.getMissingTextureObject()
                : this.textureObject;
        McConnector.client().glGraphicsManager.setShaderTexture(texture);
        shapes.drawAndRender(drawContextWrapper, modelPosTransformer, alpha);
    }
}
