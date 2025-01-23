package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    private static final Function<ResourceLocation, RenderType> QUADS = Util.memoize(texture ->
            RenderType.create("bteterrarenderer-quads", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, true, generateParameters(texture)));

    private static final Function<ResourceLocation, RenderType> TRIS = Util.memoize(texture ->
            RenderType.create("bteterrarenderer-tris", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES, 1536, true, true, generateParameters(texture)));

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture) {
        ResourceLocation id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = QUADS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsQuad<PosTex> shape, McCoordTransformer modelPosTransformer, float alpha) {
                PoseStack.Pose entry = ((WorldDrawContextWrapperImpl) context).stack().last();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEach(v -> nextVertex(entry, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture) {
        ResourceLocation id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = TRIS.apply(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsTriangle<PosTexNorm> shape, McCoordTransformer modelPosTransformer, float alpha) {
                PoseStack.Pose entry = ((WorldDrawContextWrapperImpl) context).stack().last();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEach(v -> nextVertex(entry, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    private static void nextVertex(PoseStack.Pose entry, VertexConsumer consumer, McCoord pos, float u, float v, float alpha) {
        consumer.vertex(entry.pose(), (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0x00F000F0)
                // Fixing the normal value to (0, 1, 0) will give us the full brightness.
                // So even though PosTexNorm has normal values, we will ignore it or
                // otherwise the models will have somewhat "random" brightness values,
                // making the models look weird.
                .normal(entry.normal(), 0, 1, 0)
                .endVertex();
    }

    private static RenderType.CompositeState generateParameters(ResourceLocation texture) {
        return RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, true, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true);
    }
}
