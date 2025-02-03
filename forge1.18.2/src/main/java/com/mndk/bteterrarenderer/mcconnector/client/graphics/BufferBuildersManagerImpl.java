package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

import java.util.function.BiFunction;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    private static RenderType.CompositeState generateParameters(ResourceLocation texture, boolean cull) {
        return RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, true, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(cull ? RenderStateShard.CULL : RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true);
    }

    private static final BiFunction<ResourceLocation, Boolean, RenderType> QUADS = Util.memoize((texture, cull) -> RenderType.create(
            "bteterrarenderer-quads", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 1536, true, true, generateParameters(texture, cull)
    ));

    private static final BiFunction<ResourceLocation, Boolean, RenderType> TRIS = Util.memoize((texture, cull) -> RenderType.create(
            "bteterrarenderer-tris", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES, 1536, true, true, generateParameters(texture, cull)
    ));

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture, float alpha, boolean cull) {
        ResourceLocation id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = QUADS.apply(id, cull);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new QuadBufferBuilderWrapper<>() {
            private PoseStack.Pose entry;
            private VertexConsumer consumer;
            public void setContext(WorldDrawContextWrapper context) {
                this.entry = ((WorldDrawContextWrapperImpl) context).stack().last();
                this.consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
            }
            public void next(PosTex vertex) {
                McCoord tp = this.getTransformer().transform(vertex.pos);
                nextVertex(entry, consumer, tp, vertex.tex, new McCoord(0, 1, 0), alpha);
            }
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture,
                                                                         float alpha, boolean enableNormal, boolean cull) {
        ResourceLocation id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = TRIS.apply(id, cull);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new TriangleBufferBuilderWrapper<>() {
            private PoseStack.Pose entry;
            private VertexConsumer consumer;
            public void setContext(WorldDrawContextWrapper context) {
                this.entry = ((WorldDrawContextWrapperImpl) context).stack().last();
                this.consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
            }
            public void next(PosTexNorm vertex) {
                PosTexNorm tv = vertex.transform(this.getTransformer());
                nextVertex(entry, consumer, tv.pos, tv.tex, enableNormal ? tv.normal : new McCoord(0, 1, 0), alpha);
            }
        };
    }

    private static void nextVertex(PoseStack.Pose entry, VertexConsumer consumer,
                                   McCoord pos, Vector2f tex, McCoord normal, float alpha) {
        consumer.vertex(entry.pose(), (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .uv(tex.x, tex.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0x00F000F0)
                .normal(entry.normal(), (float) normal.getX(), normal.getY(), (float) normal.getZ())
                .endVertex();
    }
}
