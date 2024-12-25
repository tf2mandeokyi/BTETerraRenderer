package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        MatrixStack poseStack = drawContextWrapper.get();
        getThisWrapped().vertex(poseStack.peek().getPositionMatrix(), x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        MatrixStack poseStack = drawContextWrapper.get();
        getThisWrapped().normal(poseStack.peek().getNormalMatrix(), x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getThisWrapped().texture(u, v);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> color(float r, float g, float b, float a) {
        getThisWrapped().color(r, g, b, a);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> light(int light) {
        getThisWrapped().light(light);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> defaultOverlay() {
        getThisWrapped().overlay(OverlayTexture.DEFAULT_UV);
        return this;
    }
    public void next() {
        getThisWrapped().next();
    }

    public void drawAndRender() {
        BufferRenderer.drawWithGlobalProgram(getThisWrapped().end());
    }

}
