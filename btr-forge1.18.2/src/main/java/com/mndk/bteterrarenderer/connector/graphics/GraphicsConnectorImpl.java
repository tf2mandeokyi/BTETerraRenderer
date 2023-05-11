package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import com.mndk.bteterrarenderer.tile.TileQuad;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.SneakyThrows;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ConnectorImpl
@SuppressWarnings("unused")
public class GraphicsConnectorImpl extends RenderStateShard implements GraphicsConnector {

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
    public static PoseStack POSE_STACK = null;

    public GraphicsConnectorImpl() {
        super("", () -> {}, () -> {});
    }

    @SneakyThrows
    public int allocateAndUploadTileTexture(BufferedImage image) {
        // TODO: Find out why this doesn't work
        NativeImage nativeImage = NativeImage.read(imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation resLocation = Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
        RES_LOC_MAP.put(texture.getId(), resLocation);
        return texture.getId();
    }

    @Override
    public void drawTileQuad(TileQuad tileQuad) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = TILE_RENDER_TYPE.apply(RES_LOC_MAP.get(tileQuad.glId));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = POSE_STACK.last();
        for (int i = 0; i < 4; i++) {
            TileQuad.VertexInfo vertexInfo = tileQuad.vertices[i];
            vertexConsumer.vertex(pose.pose(), vertexInfo.x, vertexInfo.y, vertexInfo.z)
                    .uv(vertexInfo.u, vertexInfo.v)
                    .color(vertexInfo.r, vertexInfo.g, vertexInfo.b, vertexInfo.a)
                    .endVertex();
        }
        bufferSource.endBatch();
    }

    public void glBindTileTexture(int glId) {
        RenderSystem.setShaderTexture(0, RES_LOC_MAP.get(glId));
    }
    public void glDeleteTileTexture(int glId) {
        Minecraft.getInstance().getTextureManager().release(RES_LOC_MAP.get(glId));
        RES_LOC_MAP.remove(glId);
    }

    public void glPushAttrib() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
    }
    public void glPopAttrib() {
        GL11.glPopAttrib();
    }
    public void glTranslate(float x, float y, float z) {
        POSE_STACK.translate(x, y, z);
    }
    public void glScale(float x, float y, float z) {
        POSE_STACK.scale(x, y, z);
    }
    public void glColor(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }
    public void glPushMatrix() {
        POSE_STACK.pushPose();
    }
    public void glPopMatrix() {
        POSE_STACK.popPose();
    }
    public void glEnableScissorTest() {
        GlStateManager._enableScissorTest();
    }
    public void glDisableScissorTest() {
        GlStateManager._disableScissorTest();
    }
    public void glEnableCull() {
        RenderSystem.enableCull();
    }
    public void glDisableCull() {
        RenderSystem.disableCull();
    }
    public void glEnableBlend() {
        RenderSystem.enableBlend();
    }
    public void glDisableBlend() {
        RenderSystem.disableBlend();
    }
    public void glEnableTexture2D() {
        GL11.glEnable(GlConst.GL_TEXTURE_2D);
    }
    public void glDisableTexture2D() {
        GL11.glDisable(GlConst.GL_TEXTURE_2D);
    }
    public void glRelativeScissor(int x, int y, int width, int height) {
        Window window = Minecraft.getInstance().getWindow();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        RenderSystem.getModelViewMatrix().store(buffer);
        buffer.rewind();

        int translateX = (int) buffer.get(12), translateY = (int) buffer.get(13);
        int scaleFactorX = window.getGuiScaledWidth() / window.getScreenWidth();
        int scaleFactorY = window.getGuiScaledHeight() / window.getScreenHeight();
        GL11.glScissor(
                scaleFactorX * (x + translateX), window.getGuiScaledHeight() - scaleFactorY * (y + translateY + height),
                scaleFactorX * width, scaleFactorY * height
        );
    }
    public void glBlendFunc(GlFactor srcFactor, GlFactor dstFactor) {
        RenderSystem.blendFunc(srcFactor.srcFactor, dstFactor.dstFactor);
    }
    public void glTryBlendFuncSeparate(GlFactor srcFactor, GlFactor dstFactor, GlFactor srcFactorAlpha, GlFactor dstFactorAlpha) {
        RenderSystem.blendFuncSeparate(srcFactor.srcFactor, dstFactor.dstFactor, srcFactorAlpha.srcFactor, dstFactorAlpha.dstFactor);
    }

    public IBufferBuilderImpl getBufferBuilder() {
        return new IBufferBuilderImpl(Tesselator.getInstance().getBuilder());
    }
    public void tessellatorDraw() {
        Tesselator.getInstance().end();
    }
    public void bindTexture(IResourceLocation res) {
        ResourceLocation resourceLocation = ((IResourceLocationImpl) res).delegate();
        Minecraft.getInstance().getTextureManager().bindForSetup(resourceLocation);
    }

    @SneakyThrows
    private InputStream imageToInputStream(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
