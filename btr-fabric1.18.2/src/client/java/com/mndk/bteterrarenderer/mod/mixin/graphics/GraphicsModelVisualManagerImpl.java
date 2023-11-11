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

import java.awt.image.BufferedImage;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

    private final BufferBuilder BUFFER_QUAD_PTC = new BufferBuilder(0x200000);

    public void preRender() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }

    @SneakyThrows
    public Identifier allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("bteterrarenderer-tiles", texture);
    }

    public void drawModel(MatrixStack poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {

        // Initialize buffers
        BUFFER_QUAD_PTC.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // Draw
        for(GraphicsQuad<?> quad : model.getQuads()) {
            if(quad.getVertexClass() == GraphicsQuad.PosTex.class) {
                for (int i = 0; i < 4; i++) {
                    GraphicsQuad.PosTex vertex = (GraphicsQuad.PosTex) quad.getVertex(i);
                    BUFFER_QUAD_PTC.vertex(poseStack.peek().getPositionMatrix(),
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

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, (Identifier) model.getTextureObject());

        BUFFER_QUAD_PTC.end();
        BufferRenderer.draw(BUFFER_QUAD_PTC);
    }

    public void deleteTexture(Identifier textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject);
    }

    public void postRender() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

}
