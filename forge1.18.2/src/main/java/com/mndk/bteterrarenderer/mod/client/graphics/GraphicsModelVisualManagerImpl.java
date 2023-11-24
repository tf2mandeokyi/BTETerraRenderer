package com.mndk.bteterrarenderer.mod.client.graphics;

import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class GraphicsModelVisualManagerImpl {

    // I hate this
    private static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    private static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);

    private static final RenderStateShard.ShaderStateShard POS_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    private static final RenderStateShard.TransparencyStateShard MODEL_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("bteterrarenderer_tile_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final Function<ResourceLocation, RenderType> QUAD_PTC_MODEL = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setCullState(NO_CULL)
                .setOverlayState(OVERLAY)
                .setShaderState(POS_TEX_COLOR_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(MODEL_TRANSPARENCY)
                .createCompositeState(true);
        return RenderType.create(
                "bteterrarenderer_tile", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                256, true, false, compositeState
        );
    });
    private static final Function<ResourceLocation, RenderType> TRIANGLE_PTC_MODEL = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setCullState(NO_CULL)
                .setOverlayState(OVERLAY)
                .setShaderState(POS_TEX_COLOR_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(MODEL_TRANSPARENCY)
                .createCompositeState(true);
        return RenderType.create(
                "bteterrarenderer_tile", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES,
                256, true, false, compositeState
        );
    });

    @SneakyThrows
    public ResourceLocation allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        return Minecraft.getInstance().getTextureManager().register("bteterrarenderer-tiles", texture);
    }

    public void drawModel(PoseStack poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ResourceLocation resourceLocation = (ResourceLocation) model.getTextureObject();
        Matrix4f matrix = poseStack.last().pose();

        if(!model.getQuads().isEmpty()) {
            RenderType posTexColorRenderType = QUAD_PTC_MODEL.apply(resourceLocation);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(posTexColorRenderType);
            drawShapeList(vertexConsumer, matrix, model.getQuads(), px, py, pz, opacity);
        }
        if(!model.getTriangles().isEmpty()) {
            RenderType posTexColorRenderType = TRIANGLE_PTC_MODEL.apply(resourceLocation);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(posTexColorRenderType);
            drawShapeList(vertexConsumer, matrix, model.getTriangles(), px, py, pz, opacity);
        }
    }

    private void drawShapeList(VertexConsumer vertexConsumer, Matrix4f matrix, List<? extends GraphicsShape<?>> shapes, double px, double py, double pz, float opacity) {
        for(GraphicsShape<?> shape : shapes) {
            if(shape.getVertexClass() != PosTex.class) {
                throw new UnsupportedOperationException("Not implemented");
            }

            for (int i = 0; i < shape.getVerticesCount(); i++) {
                PosTex vertex = (PosTex) shape.getVertex(i);
                float x = (float) (vertex.x - px);
                float y = (float) (vertex.y - py);
                float z = (float) (vertex.z - pz);
                vertexConsumer.vertex(matrix, x, y, z)
                        .uv(vertex.u, vertex.v)
                        .color(1f, 1f, 1f, opacity)
                        .endVertex();
            }
        }
    }

    public void deleteTexture(ResourceLocation textureObject) {
        Minecraft.getInstance().getTextureManager().release(textureObject);
    }

    public void preRender() {}
    public void postRender() {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();
    }

}
