package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.tile.TileGraphicsConnector;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

@ConnectorImpl
@SuppressWarnings("unused")
public class TileGraphicsConnectorImpl implements TileGraphicsConnector {
    @Override
    public void preRender() {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlFactor.SRC_ALPHA.srcFactor, GlFactor.ONE_MINUS_SRC_ALPHA.dstFactor);
    }

    @Override
    public int allocateAndUploadTileTexture(BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return glId;
    }

    @Override
    public void drawTileQuad(GraphicVertices<GraphicVertices.PosTexColor> vertices) {
        GlStateManager.bindTexture(vertices.glId);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (GraphicVertices.PosTexColor vertexInfo : vertices) {
            builder.pos(vertexInfo.x, vertexInfo.y, vertexInfo.z)
                    .tex(vertexInfo.u, vertexInfo.v)
                    .color(vertexInfo.r, vertexInfo.g, vertexInfo.b, vertexInfo.a)
                    .endVertex();
        }
        tessellator.draw();
    }

    @Override
    public void glDeleteTileTexture(int glId) {
        GlStateManager.deleteTexture(glId);
    }

    @Override
    public void postRender() {
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }
}
