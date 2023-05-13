package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.tile.TileGraphicsConnector;
import com.mndk.bteterrarenderer.tile.TileQuad;
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

public class TileGraphicsConnectorImpl extends RenderStateShard implements TileGraphicsConnector {

    protected static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    private static final RenderStateShard.TransparencyStateShard TILE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("bteterrarenderer_tile_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final Function<ResourceLocation, RenderType> TILE_RENDER_TYPE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setCullState(RenderStateShard.NO_CULL)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setShaderState(POSITION_TEX_COLOR_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(TILE_TRANSPARENCY)
                .createCompositeState(true);
        return RenderType.create(
                "bteterrarenderer_tile", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                256, true, false, compositeState
        );
    });

    private static final Map<Integer, ResourceLocation> RES_LOC_MAP = new HashMap<>();

    public TileGraphicsConnectorImpl() {
        super("", () -> {}, () -> {});
    }

    @Override
    @SneakyThrows
    public int allocateAndUploadTileTexture(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation resLocation = Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
        RES_LOC_MAP.put(texture.getId(), resLocation);
        return texture.getId();
    }

    @Override
    public void drawTileQuad(TileQuad<TileQuad.PosTexColor> tileQuad) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = TILE_RENDER_TYPE.apply(RES_LOC_MAP.get(tileQuad.glId));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = GraphicsConnectorImpl.POSE_STACK.last();

        for (TileQuad.PosTexColor vertexInfo : tileQuad) {
            vertexConsumer.vertex(pose.pose(), vertexInfo.x, vertexInfo.y, vertexInfo.z)
                    .uv(vertexInfo.u, vertexInfo.v)
                    .color(vertexInfo.r, vertexInfo.g, vertexInfo.b, vertexInfo.a)
                    .endVertex();
        }
        bufferSource.endBatch();
    }

    @Override
    public void glDeleteTileTexture(int glId) {
        Minecraft.getInstance().getTextureManager().release(RES_LOC_MAP.get(glId));
        RES_LOC_MAP.remove(glId);
    }

    @Override public void preRender() {}
    @Override public void postRender() {}

    @SneakyThrows
    private InputStream imageToInputStream(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
