package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordAABB;
import com.mndk.bteterrarenderer.util.BTRUtil;

public class GraphicsQuad<T extends GraphicsVertex<T>> extends GraphicsShape<T> {

    private final Object[] vertices;

    private GraphicsQuad(Class<T> vertexClass) {
        super(vertexClass);
        this.vertices = new Object[4];
    }

    private GraphicsQuad(Class<T> vertexClass, T v0, T v1, T v2, T v3) {
        super(vertexClass);
        this.vertices = new Object[] { v0, v1, v2, v3 };
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
        return 4;
    }

    @Override
    public McCoordAABB getBoundingBox() {
        return new McCoordAABB(this.getVertex(0).getMcCoord())
                .include(this.getVertex(1).getMcCoord())
                .include(this.getVertex(2).getMcCoord())
                .include(this.getVertex(3).getMcCoord());
    }

    public static GraphicsQuad<PosTex> newPosTex() {
        return new GraphicsQuad<>(PosTex.class);
    }

    public static GraphicsQuad<PosXY> newPosXY(PosXY v0, PosXY v1, PosXY v2, PosXY v3) {
        return new GraphicsQuad<>(PosXY.class, v0, v1, v2, v3);
    }

    @Override
    public String toString() {
        return String.format("GraphicsQuad[%s, %s, %s, %s]", vertices[0], vertices[1], vertices[2], vertices[3]);
    }
}
