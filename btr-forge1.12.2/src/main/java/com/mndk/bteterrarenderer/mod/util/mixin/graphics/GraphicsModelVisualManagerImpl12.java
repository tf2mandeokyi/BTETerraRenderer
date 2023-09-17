package com.mndk.bteterrarenderer.mod.util.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class GraphicsModelVisualManagerImpl12 {

    public void preRender() {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public AtomicInteger allocateAndGetTextureObject(BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return new AtomicInteger(glId);
    }

    public void drawModel(GraphicsModel model, double px, double py, double pz, float opacity) {
        int glId = ((AtomicInteger) model.getTextureObject()).get();
        GlStateManager.bindTexture(glId);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for(GraphicsQuad<?> quad : model.getQuads()) {
            if(quad.getVertexClass() == GraphicsQuad.PosTex.class) {
                for (int i = 0; i < 4; i++) {
                    GraphicsQuad.PosTex vertex = (GraphicsQuad.PosTex) quad.getVertex(i);
                    builder.pos(vertex.x - px, vertex.y - py, vertex.z - pz)
                            .tex(vertex.u, vertex.v)
                            .color(1f, 1f, 1f, opacity)
                            .endVertex();
                }
            }
            else {
                // TODO
                throw new UnsupportedOperationException("Not implemented");
            }
        }
        tessellator.draw();
    }

    public void deleteTexture(Object textureObject) {
        int glId = ((AtomicInteger) textureObject).get();
        GlStateManager.deleteTexture(glId);
    }

    public void postRender() {
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }
}
