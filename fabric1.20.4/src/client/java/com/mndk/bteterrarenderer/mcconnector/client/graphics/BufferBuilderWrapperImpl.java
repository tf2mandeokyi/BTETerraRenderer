package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public void beginPTCQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }
    public void beginPTCTriangles() {
        getThisWrapped().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
    }
    public void ptc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        getThisWrapped().vertex(matrix4f, x, y, z).texture(u, v).color(r, g, b, a).next();
    }

    public void beginPCQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    }
    public void pc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float r, float g, float b, float a) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        getThisWrapped().vertex(matrix4f, x, y, z).color(r, g, b, a).next();
    }

    public void beginPTQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
    }
    public void pt(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        getThisWrapped().vertex(matrix4f, x, y, z).texture(u, v).next();
    }

    public void beginPQuads() {
        getThisWrapped().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
    }
    public void p(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        DrawContext drawContext = drawContextWrapper.get();
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        getThisWrapped().vertex(matrix4f, x, y, z).next();
    }

    public void drawAndRender() {
        BufferRenderer.drawWithGlobalProgram(getThisWrapped().end());
    }

}
