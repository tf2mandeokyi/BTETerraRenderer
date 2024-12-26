package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends AbstractBufferBuilderWrapper<BufferBuilder> {

    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public AbstractBufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        getWrapped().pos(x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        getWrapped().normal(x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getWrapped().tex(u, v);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> color(float r, float g, float b, float a) {
        getWrapped().color(r, g, b, a);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> light(int light) {
        getWrapped().lightmap(light & '\uffff', light >> 16 & '\uffff');
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> defaultOverlay() {
        return this;
    }
    public void next() {
        getWrapped().endVertex();
    }

    public void drawAndRender() {
        getWrapped().finishDrawing();
        this.vboUploader.draw(getWrapped());
    }
}
