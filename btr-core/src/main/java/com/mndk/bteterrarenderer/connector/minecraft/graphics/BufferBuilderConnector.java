package com.mndk.bteterrarenderer.connector.minecraft.graphics;

public interface BufferBuilderConnector {
    void begin(int glMode, VertexFormat vertexFormat);
    BufferBuilderConnector pos(double x, double y, double z);
    BufferBuilderConnector tex(double u, double v);
    BufferBuilderConnector color(float red, float green, float blue, float opacity);
    void endVertex();
}
