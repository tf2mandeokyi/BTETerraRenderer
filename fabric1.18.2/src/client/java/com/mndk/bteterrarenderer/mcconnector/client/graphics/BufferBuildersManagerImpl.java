package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

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

    private static final Function<Identifier, RenderLayer> QUADS = Util.memoize(texture -> RenderLayer.of(
            "bteterrarenderer-quads", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 1536, true, true, generateParameters(texture)
    ));

    private static final Function<Identifier, RenderLayer> TRIS = Util.memoize(texture -> RenderLayer.of(
            "bteterrarenderer-tris", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.TRIANGLES, 1536, true, true, generateParameters(texture)
    ));

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture, float alpha) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = QUADS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new QuadBufferBuilderWrapper<>() {
            private MatrixStack.Entry entry;
            private VertexConsumer consumer;
            public void setContext(WorldDrawContextWrapper context) {
                this.entry = ((WorldDrawContextWrapperImpl) context).stack().peek();
                this.consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
            }
            public void next(PosTex vertex) {
                McCoord tp = this.getTransformer().transform(vertex.pos);
                nextVertex(entry, consumer, tp, new McCoord(0, 1, 0), vertex.u, vertex.v, alpha);
            }
            public void drawAndRender() {}
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture,
                                                                         float alpha, boolean enableNormal) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = TRIS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new TriangleBufferBuilderWrapper<>() {
            private MatrixStack.Entry entry;
            private VertexConsumer consumer;
            public void setContext(WorldDrawContextWrapper context) {
                this.entry = ((WorldDrawContextWrapperImpl) context).stack().peek();
                this.consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
            }
            public void next(PosTexNorm vertex) {
                PosTexNorm tv = vertex.transform(this.getTransformer());
                nextVertex(entry, consumer, tv.pos, enableNormal ? tv.normal : new McCoord(0, 1, 0), tv.u, tv.v, alpha);
            }
            public void drawAndRender() {}
        };
    }

    private static void nextVertex(MatrixStack.Entry entry, VertexConsumer consumer, McCoord pos, McCoord normal,
                                   float u, float v, float alpha) {
        consumer.vertex(entry.getPositionMatrix(), (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(0x00F000F0)
                .normal(entry.getNormalMatrix(), (float) normal.getX(), normal.getY(), (float) normal.getZ())
                .next();
    }
}
