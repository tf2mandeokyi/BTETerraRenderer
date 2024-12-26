package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends AbstractBufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public AbstractBufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        MatrixStack poseStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        getWrapped().vertex(poseStack.peek().getPositionMatrix(), x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        MatrixStack poseStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        getWrapped().normal(poseStack.peek().getNormalMatrix(), x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getWrapped().texture(u, v);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> color(float r, float g, float b, float a) {
        getWrapped().color(r, g, b, a);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> light(int light) {
        getWrapped().light(light);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> defaultOverlay() {
        getWrapped().overlay(OverlayTexture.DEFAULT_UV);
        return this;
    }
    public void next() {
        getWrapped().next();
    }

    public void drawAndRender() {
        getWrapped().end();
        BufferRenderer.draw(getWrapped());
    }

}
