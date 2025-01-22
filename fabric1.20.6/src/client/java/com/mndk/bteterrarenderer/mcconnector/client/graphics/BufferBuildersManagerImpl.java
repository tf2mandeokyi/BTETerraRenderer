package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsQuad<PosTex> shape, McCoordTransformer modelPosTransformer, float alpha) {
                MatrixStack stack = ((WorldDrawContextWrapperImpl) context).stack();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEach(v -> nextVertex(stack, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture) {
        Identifier id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(id);

        // DrawMode.QUADS
        // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        return new BufferBuilderWrapper<>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsTriangle<PosTexNorm> shape, McCoordTransformer modelPosTransformer, float alpha) {
                MatrixStack stack = ((WorldDrawContextWrapperImpl) context).stack();
                VertexConsumer consumer = ((WorldDrawContextWrapperImpl) context).provider().getBuffer(renderLayer);
                shape.forEachAsQuad(v -> nextVertex(stack, consumer, modelPosTransformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {}
        };
    }

    private void nextVertex(MatrixStack stack, VertexConsumer consumer, McCoord pos, float u, float v, float alpha) {
        consumer.vertex(stack.peek(), (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(0x00F000F0)
                // Fixing the normal value to (0, 1, 0) will give us the full brightness.
                // So even though PosTexNorm has normal values, we will ignore it or
                // otherwise the models will have somewhat "random" brightness values,
                // making the models look weird.
                .normal(stack.peek(), 0, 1, 0)
                .next();
    }
}
