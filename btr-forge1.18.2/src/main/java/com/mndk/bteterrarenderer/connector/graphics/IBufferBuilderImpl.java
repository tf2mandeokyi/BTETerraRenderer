package com.mndk.bteterrarenderer.connector.graphics;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IBufferBuilderImpl implements IBufferBuilder {
    private final BufferBuilder delegate;

    public void beginQuads(VertexFormatConnectorEnum vertexFormat) {
        delegate.begin(VertexFormat.Mode.QUADS, VertexFormatImpl.toMinecraftVertexFormat(vertexFormat));
    }
    public IBufferBuilderImpl pos(double x, double y, double z) {
        delegate.vertex(GraphicsConnectorImpl.POSE_STACK.last().pose(), (float) x, (float) y, (float) z);
        return this;
    }
    public IBufferBuilderImpl tex(float u, float v) {
        delegate.uv(u, v);
        return this;
    }
    public IBufferBuilderImpl color(float red, float green, float blue, float alpha) {
        delegate.color(red, green, blue, alpha);
        return this;
    }
    public void endVertex() {
        delegate.endVertex();
    }
}
