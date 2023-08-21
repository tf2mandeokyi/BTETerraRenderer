package com.mndk.bteterrarenderer.mod.util.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.SneakyThrows;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModelGraphicsManagerImpl18 extends RenderStateShard {

    protected static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    private static final RenderStateShard.TransparencyStateShard MODEL_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("bteterrarenderer_tile_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final Function<ResourceLocation, RenderType> MODEL_RENDER_TYPE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setCullState(RenderStateShard.NO_CULL)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setShaderState(POSITION_TEX_COLOR_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(MODEL_TRANSPARENCY)
                .createCompositeState(true);
        return RenderType.create(
                "bteterrarenderer_tile", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                256, true, false, compositeState
        );
    });

    private static final Map<Integer, ResourceLocation> RES_LOC_MAP = new HashMap<>();

    public ModelGraphicsManagerImpl18() {
        super("", () -> {}, () -> {});
    }

    @SneakyThrows
    public static int allocateAndUploadTexture(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation resLocation = Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
        RES_LOC_MAP.put(texture.getId(), resLocation);
        return texture.getId();
    }

    public static void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = MODEL_RENDER_TYPE.apply(RES_LOC_MAP.get(model.getTextureGlId()));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        for(GraphicsQuad<GraphicsQuad.PosTexColor> quad : model.getQuads()) {
            for (int i = 0; i < 4; i++) {
                GraphicsQuad.PosTexColor vertex = quad.getVertex(i);
                vertexConsumer.vertex(((PoseStack) poseStack).last().pose(),
                                (float) (vertex.x - px), (float) (vertex.y - py), (float) (vertex.z - pz))
                        .uv(vertex.u, vertex.v)
                        .color(vertex.r, vertex.g, vertex.b, vertex.a * opacity)
                        .endVertex();
            }
            bufferSource.endBatch();
        }
    }

    public static void glDeleteTexture(int glId) {
        Minecraft.getInstance().getTextureManager().release(RES_LOC_MAP.get(glId));
        RES_LOC_MAP.remove(glId);
    }

    public static void preRender() {}
    public static void postRender() {}

    @SneakyThrows
    private static InputStream imageToInputStream(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
