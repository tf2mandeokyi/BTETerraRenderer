package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DrawingFormat<T extends GraphicsVertex<T>, U extends GraphicsShape<T>>
        implements ShaderSetter, VertexBeginner, VertexConsumer<T> {

    public static final DrawingFormat<PosTex, GraphicsQuad<PosTex>> QUAD_PT = new DrawingFormat<>(
            GlGraphicsManager::setPosTexColorShader,
            DrawModeEnum.QUADS, VertexFormatEnum.POSITION_TEXTURE_COLOR,
            (context, builder, vertex, alpha) ->
                    builder.nextPosTex(context, vertex, alpha)
    );

    // TODO(fabric1.20.4): Fix holograms rendering over world
    public static final DrawingFormat<PosTexNorm, GraphicsQuad<PosTexNorm>> QUAD_PTN = new DrawingFormat<>(
            GlGraphicsManager::setPosColorTexLightNormalShader,
            DrawModeEnum.QUADS, VertexFormatEnum.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            (context, builder, vertex, alpha) ->
                    builder.nextPosTexNorm(context, vertex, alpha)
    );

    private final ShaderSetter shaderSetter;
    private final DrawModeEnum drawMode;
    private final VertexFormatEnum vertexFormat;
    private final VertexConsumer<T> vertexConsumer;

    public void nextShape(DrawContextWrapper drawContextWrapper, BufferBuilderWrapper builder,
                          U shape, McCoordTransformer mcCoordTransformer, float alpha) {
        for (int i = 0; i < shape.getVerticesCount(); i++) {
            T vertex = shape.getVertex(i).transformMcCoord(mcCoordTransformer);
            this.nextVertex(drawContextWrapper, builder, vertex, alpha);
        }
    }

    public void setShader(GlGraphicsManager glGraphicsManager) {
        shaderSetter.setShader(glGraphicsManager);
    }
    public BufferBuilderWrapper begin(DrawContextWrapper drawContextWrapper) {
        return drawContextWrapper.begin(this.drawMode, this.vertexFormat);
    }
    public void nextVertex(DrawContextWrapper drawContextWrapper, BufferBuilderWrapper builder,
                           T vertex, float alpha) {
        vertexConsumer.nextVertex(drawContextWrapper, builder, vertex, alpha);
    }
}
