package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture, float alpha, boolean cull) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // GL11.GL_QUADS
        // DefaultVertexFormats.POSITION_COLOR_TEX_LIGHT
        return new QuadBufferBuilderWrapper<PosTex>() {
            public void setContext(WorldDrawContextWrapper context) {}
            public void preUpload() {
                GlStateManager.disableCull();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.bindTexture(((NativeTextureWrapperImpl) texture).delegate);
                bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            }
            public void next(PosTex vertex) {
                nextVertex(bufferBuilder, this.getTransformer().transform(vertex.pos), vertex.tex, alpha);
            }
            public void upload() {
                tessellator.draw();
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }
        };
    }

    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture,
                                                                         float alpha, boolean enableNormal) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // GL11.GL_TRIANGLES
        // DefaultVertexFormats.POSITION_COLOR_TEX_LIGHT
        return new TriangleBufferBuilderWrapper<PosTexNorm>() {
            public void setContext(WorldDrawContextWrapper context) {}
            public void preUpload() {
                GlStateManager.disableCull();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.bindTexture(((NativeTextureWrapperImpl) texture).delegate);

                bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.BLOCK);
            }
            public void next(PosTexNorm vertex) {
                nextVertex(bufferBuilder, this.getTransformer().transform(vertex.pos), vertex.tex, alpha);
            }
            public void upload() {
                tessellator.draw();
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }
        };
    }

    private static void nextVertex(BufferBuilder bufferBuilder, McCoord pos, Vector2f tex, float alpha) {
        bufferBuilder.pos((float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .tex(tex.x, tex.y)
                .lightmap(0xf0, 0xf0)
                .endVertex();
    }
}
