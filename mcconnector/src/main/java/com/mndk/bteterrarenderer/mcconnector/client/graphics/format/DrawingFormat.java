package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

public interface DrawingFormat<T extends GraphicsVertex<T>, U extends GraphicsShape<T>>
        extends ShaderSetter, VertexBeginner, VertexConsumer<T> {

    DrawingFormat<PosTex, GraphicsQuad<PosTex>> QUAD_PT_ALPHA = DrawingFormat.of(
            GlGraphicsManager::setPositionTexColorShader,
            DrawContextWrapper::beginPtcQuads,
            (context, builder, vertex, alpha) -> builder
                    .position(context, vertex.pos.getX(), vertex.pos.getY(), vertex.pos.getZ())
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .next()
    );

    DrawingFormat<PosTexNorm, GraphicsTriangle<PosTexNorm>> TRI_PTN_ALPHA = DrawingFormat.of(
            GlGraphicsManager::setPositionTexColorNormalShader,
            DrawContextWrapper::beginPtcnTriangles,
            (context, builder, vertex, alpha) -> builder
                    .position(context, vertex.pos.getX(), vertex.pos.getY(), vertex.pos.getZ())
                    .texture(vertex.u, vertex.v)
                    .color(1, 1, 1, alpha)
                    .normal(context, vertex.normal.getX(), vertex.normal.getY(), vertex.normal.getZ())
                    .next()
    );

    default void nextShape(DrawContextWrapper<?> drawContextWrapper, BufferBuilderWrapper<?> builder,
                           U shape, McCoordTransformer mcCoordTransformer, float alpha) {
        for (int i = 0; i < shape.getVerticesCount(); i++) {
            T vertex = shape.getVertex(i).transformMcCoord(mcCoordTransformer);
            this.nextVertex(drawContextWrapper, builder, vertex, alpha);
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
            public BufferBuilderWrapper<?> begin(DrawContextWrapper<?> drawContextWrapper) {
                return beginner.begin(drawContextWrapper);
            }
            public void nextVertex(DrawContextWrapper<?> drawContextWrapper, BufferBuilderWrapper<?> builder,
                                   T vertex, float alpha) {
                vertexConsumer.nextVertex(drawContextWrapper, builder, vertex, alpha);
            }
        };
    }
}
