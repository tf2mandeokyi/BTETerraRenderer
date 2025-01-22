package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GraphicsModel {
    private final NativeTextureWrapper textureObject;
    private final GraphicsShapes shapes;

    public void drawAndRender(WorldDrawContextWrapper context, McCoordTransformer modelPosTransformer, float alpha) {
        NativeTextureWrapper texture = this.textureObject.isDeleted()
                ? McConnector.client().glGraphicsManager.getMissingTextureObject()
                : this.textureObject;
        shapes.drawAndRender(context, texture, modelPosTransformer, alpha);
    }
}
