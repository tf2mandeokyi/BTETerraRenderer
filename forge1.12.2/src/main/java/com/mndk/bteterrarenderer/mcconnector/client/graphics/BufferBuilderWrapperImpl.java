package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
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
    public BufferBuilderWrapper<BufferBuilder> light(int light) {
        getThisWrapped().lightmap(light & '\uffff', light >> 16 & '\uffff');
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> defaultOverlay() {
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
