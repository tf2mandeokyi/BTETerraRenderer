package com.mndk.bteterrarenderer.core.graphics.shape;

import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.format.VertexInfo;
import com.mndk.bteterrarenderer.core.util.BTRUtil;

public class GraphicsTriangle<T extends VertexInfo> extends GraphicsShape<T> {

    private final Object[] vertices;

    private GraphicsTriangle(Class<T> vertexClass, T v0, T v1, T v2) {
        super(vertexClass);
        this.vertices = new Object[] { v0, v1, v2 };
    }

    @Override
    public T getVertex(int index) {
        return BTRUtil.uncheckedCast(vertices[index]);
    }

    @Override
    public void setVertex(int index, T value) {
        vertices[index] = value;
    }

    @Override
    public int getVerticesCount() {
        return 3;
    }

    public static GraphicsTriangle<PosTex> newPosTex(PosTex v0, PosTex v1, PosTex v2) {
        return new GraphicsTriangle<>(PosTex.class, v0, v1, v2);
    }
}
