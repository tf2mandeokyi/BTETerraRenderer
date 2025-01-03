package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
        getThisWrapped().vertex(matrix, x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix3f matrix = drawContext.getMatrices().peek().getNormalMatrix();
        getThisWrapped().normal(matrix, x, y, z);
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
