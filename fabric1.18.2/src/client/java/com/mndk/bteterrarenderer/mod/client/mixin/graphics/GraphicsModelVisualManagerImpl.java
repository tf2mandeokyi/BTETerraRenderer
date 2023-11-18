package com.mndk.bteterrarenderer.mod.client.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
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
import net.minecraft.util.math.Matrix4f;

import java.awt.image.BufferedImage;
import java.util.List;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

    private final BufferBuilder BUFFER_BUILDER = new BufferBuilder(0x200000);

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
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, (Identifier) model.getTextureObject());
        Matrix4f matrix = poseStack.peek().getPositionMatrix();

        if(!model.getQuads().isEmpty()) {
            BUFFER_BUILDER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            drawShapeList(matrix, model.getQuads(), px, py, pz, opacity);
            BUFFER_BUILDER.end();
            BufferRenderer.draw(BUFFER_BUILDER);
        }
        if(!model.getTriangles().isEmpty()) {
            BUFFER_BUILDER.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
            drawShapeList(matrix, model.getTriangles(), px, py, pz, opacity);
            BUFFER_BUILDER.end();
            BufferRenderer.draw(BUFFER_BUILDER);
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
                        .texture(vertex.u, vertex.v)
                        .color(1f, 1f, 1f, opacity)
                        .next();
            }
        }
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
