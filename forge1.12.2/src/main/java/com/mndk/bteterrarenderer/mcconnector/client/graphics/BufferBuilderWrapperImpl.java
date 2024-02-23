package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public void beginPTCQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
    }
    public void beginPTCTriangles() {
        getThisWrapped().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
    }
    public void ptc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        getThisWrapped().pos(x, y, z).tex(u, v).color(1f, 1f, 1f, a).endVertex();
    }

    public void beginPCQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    }
    public void pc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float r, float g, float b, float a) {
        getThisWrapped().pos(x, y, z).color(1f, 1f, 1f, a).endVertex();
    }

    public void beginPTQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    }
    public void pt(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v) {
        getThisWrapped().pos(x, y, z).tex(u, v).endVertex();
    }

    public void beginPQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    }
    public void p(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        getThisWrapped().pos(x, y, z).endVertex();
    }

    public void drawAndRender() {
        getThisWrapped().finishDrawing();
        this.vboUploader.draw(getThisWrapped());
    }
}
