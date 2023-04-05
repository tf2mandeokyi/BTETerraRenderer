package com.mndk.bteterrarenderer.connector.minecraft.graphics;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.BufferBuilder;

@RequiredArgsConstructor
public class BufferBuilderConnectorImpl implements BufferBuilderConnector {
    private final BufferBuilder bufferBuilder;

    public void begin(int glMode, VertexFormatConnectorEnum vertexFormat) {
        bufferBuilder.begin(glMode, VertexFormatImpl.toMinecraftVertexFormat(vertexFormat));
    }
    public BufferBuilderConnectorImpl pos(double x, double y, double z) {
        bufferBuilder.pos(x, y, z);
        return this;
    }
    public BufferBuilderConnectorImpl tex(double u, double v) {
        bufferBuilder.tex(u, v);
        return this;
    }
    public BufferBuilderConnectorImpl color(float red, float green, float blue, float alpha) {
        bufferBuilder.color(red, green, blue, alpha);
        return this;
    }
    public void endVertex() {
        bufferBuilder.endVertex();
    }
}
