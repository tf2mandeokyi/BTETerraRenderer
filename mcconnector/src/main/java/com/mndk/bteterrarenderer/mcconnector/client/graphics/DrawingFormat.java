package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawingFormat<S extends GraphicsShape> {

    public static final DrawingFormat<GraphicsQuad<PosTex>> QUAD_PT = new DrawingFormat<>(
            texture -> McConnector.client().bufferBuildersManager.begin3dQuad(texture));

    public static final DrawingFormat<GraphicsTriangle<PosTexNorm>> TRI_PTN = new DrawingFormat<>(
            texture -> McConnector.client().bufferBuildersManager.begin3dTri(texture));

    private final Function<NativeTextureWrapper, BufferBuilderWrapper<S>> bufferBuilderProvider;

    public BufferBuilderWrapper<S> begin(NativeTextureWrapper texture) {
        return bufferBuilderProvider.apply(texture);
    }
}
