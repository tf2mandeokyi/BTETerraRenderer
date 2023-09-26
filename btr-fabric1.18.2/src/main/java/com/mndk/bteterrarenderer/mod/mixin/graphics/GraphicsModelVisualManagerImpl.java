package com.mndk.bteterrarenderer.mod.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.image.BufferedImage;
import java.util.function.Function;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

    private final RenderPhase.Shader POS_TEX_COLOR_SHADER = new RenderPhase.Shader(GameRenderer::getPositionTexColorShader);
    private final RenderPhase.Transparency MODEL_TRANSPARENCY = new RenderPhase.Transparency("bteterrarenderer_tile_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private final Function<Identifier, RenderLayer> POS_TEX_COLOR_MODEL = Util.memoize(identifier -> {
        RenderLayer.MultiPhaseParameters textures = RenderLayer.MultiPhaseParameters.builder()
                .cull(RenderPhase.DISABLE_CULLING)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .shader(POS_TEX_COLOR_SHADER)
                .texture(new RenderPhase.Texture(identifier, false, false))
                .transparency(MODEL_TRANSPARENCY)
                .build(true);
        return RenderLayer.of(
                "bteterrarenderer_tile", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS,
                0x100000, true, false, textures
        );
    });

    @SneakyThrows
    public Identifier allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("bteterrarenderer-tiles", texture);
    }

    public void drawModel(MatrixStack poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        VertexConsumerProvider.Immediate vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        Identifier identifier = (Identifier) model.getTextureObject();
        RenderLayer posTexColorRenderLayer = POS_TEX_COLOR_MODEL.apply(identifier);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(posTexColorRenderLayer);
        for(GraphicsQuad<?> quad : model.getQuads()) {
            if(quad.getVertexClass() == GraphicsQuad.PosTex.class) {
                for (int i = 0; i < 4; i++) {
                    GraphicsQuad.PosTex vertex = (GraphicsQuad.PosTex) quad.getVertex(i);
                    vertexConsumer.vertex(poseStack.peek().getPositionMatrix(),
                                    (float) (vertex.x - px), (float) (vertex.y - py), (float) (vertex.z - pz))
                            .texture(vertex.u, vertex.v)
                            .color(1f, 1f, 1f, opacity)
                            .next();
                }
            }
            else {
                // TODO
                throw new UnsupportedOperationException("Not implemented");
            }
        }
        vertexConsumers.draw();
    }

    public void deleteTexture(Identifier textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject);
    }

    public void preRender() {}
    public void postRender() {}

}
