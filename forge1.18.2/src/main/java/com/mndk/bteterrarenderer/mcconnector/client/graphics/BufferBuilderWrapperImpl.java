package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.texture.OverlayTexture;

import javax.annotation.Nonnull;

public class BufferBuilderWrapperImpl extends AbstractBufferBuilderWrapper<BufferBuilder> {

    public BufferBuilderWrapperImpl(@Nonnull BufferBuilder delegate) {
        super(delegate);
    }

    public AbstractBufferBuilderWrapper<BufferBuilder> position(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        PoseStack poseStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        Matrix4f matrix = poseStack.last().pose();
        getWrapped().vertex(matrix, x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> normal(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
        PoseStack poseStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        Matrix3f matrix = poseStack.last().normal();
        getWrapped().normal(matrix, x, y, z);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> texture(float u, float v) {
        getWrapped().uv(u, v);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> color(float r, float g, float b, float a) {
        getWrapped().color(r, g, b, a);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> light(int light) {
        getWrapped().uv2(light);
        return this;
    }
    public AbstractBufferBuilderWrapper<BufferBuilder> defaultOverlay() {
        getWrapped().overlayCoords(OverlayTexture.NO_OVERLAY);
        return this;
    }
    public void next() {
        getWrapped().endVertex();
    }

    public void drawAndRender() {
        getWrapped().end();
        BufferUploader.end(getWrapped());
    }
}
