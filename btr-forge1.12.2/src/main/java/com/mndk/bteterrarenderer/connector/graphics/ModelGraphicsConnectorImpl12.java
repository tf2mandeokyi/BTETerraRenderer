package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.graphics.GraphicsQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

@ConnectorImpl
@SuppressWarnings("unused")
public class ModelGraphicsConnectorImpl12 implements ModelGraphicsConnector {
    @Override
    public void preRender() {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public int allocateAndUploadTexture(BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return glId;
    }

    @Override
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GlStateManager.bindTexture(model.getTextureGlId());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for(GraphicsQuad<GraphicsQuad.PosTexColor> quad : model.getQuads()) {
            for (int i = 0; i < 4; i++) {
                GraphicsQuad.PosTexColor vertex = quad.getVertex(i);
                builder.pos(vertex.x - px, vertex.y - py, vertex.z - pz)
                        .tex(vertex.u, vertex.v)
                        .color(vertex.r, vertex.g, vertex.b, vertex.a * opacity)
                        .endVertex();
            }
            tessellator.draw();
        }
    }

    @Override
    public void glDeleteTexture(int glId) {
        GlStateManager.deleteTexture(glId);
    }

    @Override
    public void postRender() {
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }
}
