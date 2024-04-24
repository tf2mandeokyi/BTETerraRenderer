package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;

public interface DrawingFormat<T extends GraphicsVertex<T>, U extends GraphicsShape<T>>
        extends ShaderSetter, VertexBeginner, VertexConsumer<T> {

    DrawingFormat<PosTex, GraphicsQuad<PosTex>> QUAD_PT_ALPHA = DrawingFormat.of(
            GlGraphicsManager::setPositionTexColorShader,
            BufferBuilderWrapper::beginPtcQuads,
            (context, vertex, alpha) -> context.tessellatorBufferBuilder()
                    .position(context, vertex.x, vertex.y, vertex.z)
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .next()
    );

    // TODO: Remove this warning suppression
    @SuppressWarnings("unused")
    DrawingFormat<PosTex, GraphicsTriangle<PosTex>> TRI_PT_ALPHA = DrawingFormat.of(
            GlGraphicsManager::setPositionTexColorShader,
            BufferBuilderWrapper::beginPtcTriangles,
            (context, vertex, alpha) -> context.tessellatorBufferBuilder()
                    .position(context, vertex.x, vertex.y, vertex.z)
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .next()
    );

    DrawingFormat<PosTexNorm, GraphicsTriangle<PosTexNorm>> TRI_PTN_ALPHA = DrawingFormat.of(
            GlGraphicsManager::setPositionTexColorNormalShader,
            BufferBuilderWrapper::beginPtcnTriangles,
            (context, vertex, alpha) -> context.tessellatorBufferBuilder()
                    .position(context, vertex.px, vertex.py, vertex.pz)
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .normal(context, vertex.nx, vertex.ny, vertex.nz)
                    .next()
    );

    default void nextShape(DrawContextWrapper<?> drawContextWrapper,
                           U shape, PositionTransformer vertexTransformer, float alpha) {
        for (int i = 0; i < shape.getVerticesCount(); i++) {
            T vertex = shape.getVertex(i).transformPosition(vertexTransformer);
            this.nextVertex(drawContextWrapper, vertex, alpha);
        }
    }

    static <T extends GraphicsVertex<T>, U extends GraphicsShape<T>>
    DrawingFormat<T, U> of(ShaderSetter shaderSetter,
                           VertexBeginner beginner,
                           VertexConsumer<T> vertexConsumer) {
        return new DrawingFormat<T, U>() {
            public void setShader(GlGraphicsManager glGraphicsManager) {
                shaderSetter.setShader(glGraphicsManager);
            }
            public void begin(BufferBuilderWrapper<?> builder) {
                beginner.begin(builder);
            }
            public void nextVertex(DrawContextWrapper<?> drawContextWrapper, T vertex, float alpha) {
                vertexConsumer.nextVertex(drawContextWrapper, vertex, alpha);
            }
        };
    }
}
