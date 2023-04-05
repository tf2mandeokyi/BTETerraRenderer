package com.mndk.bteterrarenderer.connector.minecraft.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface BufferBuilderConnector {
    BufferBuilderConnector INSTANCE = ImplFinder.search(BufferBuilderConnector.class);

    void begin(int glMode, VertexFormatConnectorEnum vertexFormat);
    BufferBuilderConnector pos(double x, double y, double z);
    BufferBuilderConnector tex(double u, double v);
    BufferBuilderConnector color(float red, float green, float blue, float alpha);
    void endVertex();
}
