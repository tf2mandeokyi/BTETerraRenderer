package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends BufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        PoseStack poseStack = drawContextWrapper.get();
        Matrix4f matrix = poseStack.last().pose();
        getThisWrapped().vertex(matrix, x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z) {
        PoseStack poseStack = drawContextWrapper.get();
        Matrix3f matrix = poseStack.last().normal();
        getThisWrapped().normal(matrix, x, y, z);
        return this;
    }
    public BufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getThisWrapped().uv(u, v);
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
        getThisWrapped().end();
        BufferUploader.end(getThisWrapped());
    }
}
