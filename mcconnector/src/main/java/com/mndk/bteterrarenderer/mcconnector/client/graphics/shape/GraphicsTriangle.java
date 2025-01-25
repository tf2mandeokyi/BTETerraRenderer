package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GraphicsTriangle<T extends GraphicsVertex> implements GraphicsShape {

    public final T v0, v1, v2;

    @Override
    public String toString() {
        return String.format("GraphicsTriangle[%s, %s, %s]", v0, v1, v2);
    }
}
