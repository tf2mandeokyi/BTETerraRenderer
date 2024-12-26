package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;

@FunctionalInterface
public
interface VertexConsumer<T extends GraphicsVertex<T>> {
    void nextVertex(DrawContextWrapper drawContextWrapper, BufferBuilderWrapper builder, T vertex, float alpha);
}
