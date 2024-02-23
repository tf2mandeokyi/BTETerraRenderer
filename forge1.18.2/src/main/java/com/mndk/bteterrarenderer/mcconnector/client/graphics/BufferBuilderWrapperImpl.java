package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mojang.blaze3d.vertex.*;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public void beginPTCQuads() {
        getThisWrapped().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }
    public void beginPTCTriangles() {
        getThisWrapped().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
    }
    public void ptc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        PoseStack poseStack = drawContextWrapper.get();
        getThisWrapped().vertex(poseStack.last().pose(), x, y, z).uv(u, v).color(r, g, b, a).endVertex();
    }

    public void beginPCQuads() {
        getThisWrapped().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    public void pc(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float r, float g, float b, float a) {
        PoseStack poseStack = drawContextWrapper.get();
        getThisWrapped().vertex(poseStack.last().pose(), x, y, z).color(r, g, b, a).endVertex();
    }

    public void beginPTQuads() {
        getThisWrapped().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }
    public void pt(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z, float u, float v) {
        PoseStack poseStack = drawContextWrapper.get();
        getThisWrapped().vertex(poseStack.last().pose(), x, y, z).uv(u, v).endVertex();
    }

    public void beginPQuads() {
        getThisWrapped().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
    }
    public void p(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        PoseStack poseStack = drawContextWrapper.get();
        getThisWrapped().vertex(poseStack.last().pose(), x, y, z).endVertex();
    }

    public void drawAndRender() {
        getThisWrapped().end();
        BufferUploader.end(getThisWrapped());
    }
}
