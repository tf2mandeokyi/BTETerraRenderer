package com.mndk.bteterrarenderer.connector.graphics;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL11;

@RequiredArgsConstructor
public class IBufferBuilderImpl implements IBufferBuilder {
    private final BufferBuilder delegate;

    public void beginQuads(VertexFormatConnectorEnum vertexFormat) {
        delegate.begin(GL11.GL_QUADS, VertexFormatImpl.toMinecraftVertexFormat(vertexFormat));
    }
    public IBufferBuilderImpl pos(double x, double y, double z) {
        delegate.pos(x, y, z);
        return this;
    }
    public IBufferBuilderImpl tex(float u, float v) {
        delegate.tex(u, v);
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
