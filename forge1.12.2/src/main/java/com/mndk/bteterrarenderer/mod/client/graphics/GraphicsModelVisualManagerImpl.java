package com.mndk.bteterrarenderer.mod.client.graphics;

import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

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

        if(!model.getQuads().isEmpty()) {
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            drawShapeList(builder, model.getQuads(), px, py, pz, opacity);
            tessellator.draw();
        }
        if(!model.getTriangles().isEmpty()) {
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
            drawShapeList(builder, model.getTriangles(), px, py, pz, opacity);
            tessellator.draw();
        }
    }

    private void drawShapeList(BufferBuilder builder, List<? extends GraphicsShape<?>> shapes, double px, double py, double pz, float opacity) {
        for(GraphicsShape<?> shape : shapes) {
            if(shape.getVertexClass() != PosTex.class) {
                throw new UnsupportedOperationException("Not implemented");
            }

            for (int i = 0; i < shape.getVerticesCount(); i++) {
                PosTex vertex = (PosTex) shape.getVertex(i);
                builder.pos(vertex.x - px, vertex.y - py, vertex.z - pz)
                        .tex(vertex.u, vertex.v)
                        .color(1f, 1f, 1f, opacity)
                        .endVertex();
            }
        }
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
