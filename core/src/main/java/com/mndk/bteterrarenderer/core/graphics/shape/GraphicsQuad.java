package com.mndk.bteterrarenderer.core.graphics.shape;

import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.format.PosXY;
import com.mndk.bteterrarenderer.core.graphics.format.VertexInfo;
import com.mndk.bteterrarenderer.core.util.BTRUtil;

public class GraphicsQuad<T extends VertexInfo> extends GraphicsShape<T> {

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

    public static GraphicsQuad<PosTex> newPosTex() {
        return new GraphicsQuad<>(PosTex.class);
    }

    public static GraphicsQuad<PosXY> newPosXY(PosXY v0, PosXY v1, PosXY v2, PosXY v3) {
        return new GraphicsQuad<>(PosXY.class, v0, v1, v2, v3);
    }
}
