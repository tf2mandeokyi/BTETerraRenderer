package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    private static final Function<Identifier, RenderLayer> QUADS = Util.memoize(texture ->
            RenderLayer.of("bteterrarenderer-quads", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS, 1536, true, true, generateParameters(texture)));

    private static final Function<Identifier, RenderLayer> TRIS = Util.memoize(texture ->
            RenderLayer.of("bteterrarenderer-tris", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.TRIANGLES, 1536, true, true, generateParameters(texture)));

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = QUADS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsQuad<PosTex> shape, McCoordTransformer modelPosTransformer, float alpha) {
                MatrixStack.Entry entry = ((WorldDrawContextWrapperImpl) context).stack().peek();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEach(v -> nextVertex(entry, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = TRIS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsTriangle<PosTexNorm> shape, McCoordTransformer modelPosTransformer, float alpha) {
                MatrixStack.Entry entry = ((WorldDrawContextWrapperImpl) context).stack().peek();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEach(v -> nextVertex(entry, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    private static void nextVertex(MatrixStack.Entry entry, VertexConsumer consumer, McCoord pos, float u, float v, float alpha) {
        consumer.vertex(entry.getPositionMatrix(), (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(0x00F000F0)
                // Fixing the normal value to (0, 1, 0) will give us the full brightness.
                // So even though PosTexNorm has normal values, we will ignore it or
                // otherwise the models will have somewhat "random" brightness values,
                // making the models look weird.
                .normal(entry.getNormalMatrix(), 0, 1, 0)
                .next();
    }

    private static RenderLayer.MultiPhaseParameters generateParameters(Identifier texture) {
        return RenderLayer.MultiPhaseParameters.builder()
                .shader(RenderPhase.ENTITY_TRANSLUCENT_SHADER)
                .texture(new RenderPhase.Texture(texture, true, true))
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderPhase.DISABLE_CULLING)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .build(true);
    }
}
