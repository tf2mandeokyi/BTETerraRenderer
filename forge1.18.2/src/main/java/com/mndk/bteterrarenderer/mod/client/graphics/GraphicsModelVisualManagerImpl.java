package com.mndk.bteterrarenderer.mod.client.graphics;

import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.List;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

    private final BufferBuilder BUFFER_BUILDER = new BufferBuilder(0x200000);

    public void preRender() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    @SneakyThrows
    public ResourceLocation allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        return Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
    }

    public void drawModel(PoseStack poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, (ResourceLocation) model.getTextureObject());
        Matrix4f matrix = poseStack.last().pose();

        if(!model.getQuads().isEmpty()) {
            BUFFER_BUILDER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            drawShapeList(matrix, model.getQuads(), px, py, pz, opacity);
            BUFFER_BUILDER.end();
            BufferUploader.end(BUFFER_BUILDER);
        }
        if(!model.getTriangles().isEmpty()) {
            BUFFER_BUILDER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
            drawShapeList(matrix, model.getTriangles(), px, py, pz, opacity);
            BUFFER_BUILDER.end();
            BufferUploader.end(BUFFER_BUILDER);
        }
    }

    private void drawShapeList(Matrix4f matrix, List<? extends GraphicsShape<?>> shapes, double px, double py, double pz, float opacity) {
        for(GraphicsShape<?> shape : shapes) {
            if(shape.getVertexClass() != PosTex.class) {
                throw new UnsupportedOperationException("Not implemented");
            }

            for (int i = 0; i < shape.getVerticesCount(); i++) {
                PosTex vertex = (PosTex) shape.getVertex(i);
                float x = (float) (vertex.x - px);
                float y = (float) (vertex.y - py);
                float z = (float) (vertex.z - pz);
                BUFFER_BUILDER.vertex(matrix, x, y, z)
                        .uv(vertex.u, vertex.v)
                        .color(1f, 1f, 1f, opacity)
                        .endVertex();
            }
        }
    }

    public void deleteTexture(ResourceLocation textureObject) {
        Minecraft.getInstance().getTextureManager().release(textureObject);
    }

    public void postRender() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

}
