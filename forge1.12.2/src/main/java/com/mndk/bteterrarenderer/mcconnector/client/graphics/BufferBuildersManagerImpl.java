package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture, float alpha) {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.bindTexture(((NativeTextureWrapperImpl) texture).delegate);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        // GL11.GL_QUADS
        // DefaultVertexFormats.POSITION_COLOR_TEX_LIGHT
        return new BufferBuilderWrapper<GraphicsQuad<PosTex>>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsQuad<PosTex> shape, McCoordTransformer transformer) {
                shape.forEach(v -> nextVertex(bufferBuilder, transformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {
                tessellator.draw();
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture,
                                                                         float alpha, boolean enableNormal) {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.bindTexture(((NativeTextureWrapperImpl) texture).delegate);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.BLOCK);

        // GL11.GL_TRIANGLES
        // DefaultVertexFormats.POSITION_COLOR_TEX_LIGHT
        return new BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>>() {
            public void nextShape(WorldDrawContextWrapper context, GraphicsTriangle<PosTexNorm> shape, McCoordTransformer transformer) {
                shape.forEach(v -> nextVertex(bufferBuilder, transformer.transform(v.pos), v.u, v.v, alpha));
            }
            public void drawAndRender(WorldDrawContextWrapper context) {
                tessellator.draw();
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }
        };
    }

    private static void nextVertex(BufferBuilder bufferBuilder, McCoord pos, float u, float v, float alpha) {
        bufferBuilder.pos((float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .tex(u, v)
                .lightmap(0xf0, 0xf0)
                .endVertex();
    }
}
