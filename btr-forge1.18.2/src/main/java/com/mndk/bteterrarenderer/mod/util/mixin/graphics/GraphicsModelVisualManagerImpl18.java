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
import java.util.function.Function;

public class GraphicsModelVisualManagerImpl18 extends RenderStateShard {

    protected static final RenderStateShard.ShaderStateShard POS_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    private static final RenderStateShard.TransparencyStateShard MODEL_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("bteterrarenderer_tile_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final Function<ResourceLocation, RenderType> POS_TEX_COLOR_MODEL = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setCullState(RenderStateShard.NO_CULL)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setShaderState(POS_TEX_COLOR_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(MODEL_TRANSPARENCY)
                .createCompositeState(true);
        return RenderType.create(
                "bteterrarenderer_tile", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                256, true, false, compositeState
        );
    });

    public GraphicsModelVisualManagerImpl18() {
        super("", () -> {}, () -> {});
    }

    @SneakyThrows
    public static Object allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        return Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
    }

    public static void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ResourceLocation resourceLocation = (ResourceLocation) model.getTextureObject();
        RenderType posTexColorRenderType = POS_TEX_COLOR_MODEL.apply(resourceLocation);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(posTexColorRenderType);
        for(GraphicsQuad<?> quad : model.getQuads()) {
            if(quad.getVertexClass() == GraphicsQuad.PosTex.class) {
                for (int i = 0; i < 4; i++) {
                    GraphicsQuad.PosTex vertex = (GraphicsQuad.PosTex) quad.getVertex(i);
                    vertexConsumer.vertex(((PoseStack) poseStack).last().pose(),
                                    (float) (vertex.x - px), (float) (vertex.y - py), (float) (vertex.z - pz))
                            .uv(vertex.u, vertex.v)
                            .color(1f, 1f, 1f, opacity)
                            .endVertex();
                }
            }
            else {
                // TODO
                throw new UnsupportedOperationException("Not implemented");
            }
        }
        bufferSource.endBatch();
    }

    public static void deleteTexture(Object textureObject) {
        Minecraft.getInstance().getTextureManager().release((ResourceLocation) textureObject);
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
