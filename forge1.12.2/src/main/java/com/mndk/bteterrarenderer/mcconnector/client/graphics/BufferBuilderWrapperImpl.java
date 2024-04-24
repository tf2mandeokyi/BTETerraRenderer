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

    public void beginPtcnTriangles() {
        getThisWrapped().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    }
    public void beginPtcQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
    }
    public void beginPtcTriangles() {
        getThisWrapped().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
    }
    public void beginPcQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    }
    public void beginPtQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    }
    public void beginPQuads() {
        getThisWrapped().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    }

    public BufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        getThisWrapped().pos(x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        getThisWrapped().normal(x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getThisWrapped().tex(u, v);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> color(float r, float g, float b, float a) {
        getThisWrapped().color(r, g, b, a);
        return this;
    }
    public void next() {
        getThisWrapped().endVertex();
    }

    public void drawAndRender() {
        getThisWrapped().finishDrawing();
        this.vboUploader.draw(getThisWrapped());
    }
}
