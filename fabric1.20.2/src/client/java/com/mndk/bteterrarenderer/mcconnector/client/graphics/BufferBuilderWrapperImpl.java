package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public void beginPtcnTriangles() {
        getThisWrapped().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
    }
    public void beginPtcQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }
    public void beginPtcTriangles() {
        getThisWrapped().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
    }
    public void beginPcQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    }
    public void beginPtQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
    }
    public void beginPQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
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
    public void next() {
        getThisWrapped().next();
    }

    public void drawAndRender() {
        BufferRenderer.drawWithGlobalProgram(getThisWrapped().end());
    }

}
