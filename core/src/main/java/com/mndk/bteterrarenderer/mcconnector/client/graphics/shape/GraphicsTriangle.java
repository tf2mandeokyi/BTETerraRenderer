package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordAABB;

public class GraphicsTriangle<T extends GraphicsVertex<T>> extends GraphicsShape<T> {

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

    @Override
    public McCoordAABB getBoundingBox() {
        return new McCoordAABB(this.getVertex(0).getMcCoord())
                .include(this.getVertex(1).getMcCoord())
                .include(this.getVertex(2).getMcCoord());
    }

    public static GraphicsTriangle<PosTexNorm> newPosTexNorm(PosTexNorm v0, PosTexNorm v1, PosTexNorm v2) {
        return new GraphicsTriangle<>(PosTexNorm.class, v0, v1, v2);
    }

    @Override
    public String toString() {
        return String.format("GraphicsTriangle[%s, %s, %s]", vertices[0], vertices[1], vertices[2]);
    }
}
