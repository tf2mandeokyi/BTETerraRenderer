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
            (context, builder, vertex, alpha) -> builder
                    .position(context, vertex.pos)
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .next()
    );

    // TODO(fabric1.21.4): Fix holograms rendering over terrain
    public static final DrawingFormat<PosTexNorm, GraphicsQuad<PosTexNorm>> QUAD_PTN = new DrawingFormat<>(
            // GlGraphicsManager::setPosColorTexLightNormalShader,
            GlGraphicsManager::setPosTexColorShader,
            DrawModeEnum.QUADS, VertexFormatEnum.POSITION_TEXTURE_COLOR, // VertexFormatEnum.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            (context, builder, vertex, alpha) -> builder
//                    .position(context, vertex.pos)
//                    .color(1, 1, 1, alpha)
//                    .texture(vertex.u, vertex.v)
//                    .light(0x00F000F0)
//                    .normal(context, vertex.normal)
//                    .next()
                    .position(context, vertex.pos)
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .next()
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
