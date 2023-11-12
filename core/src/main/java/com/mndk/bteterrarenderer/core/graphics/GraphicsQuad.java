package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class GraphicsQuad<T extends GraphicsQuad.VertexInfo> {

    @Getter
    private final Class<T> vertexClass;
    private final Object[] vertices;

    private GraphicsQuad(Class<T> vertexClass) {
        this.vertexClass = vertexClass;
        this.vertices = new Object[4];
    }

    private GraphicsQuad(Class<T> vertexClass, T v0, T v1, T v2, T v3) {
        this.vertexClass = vertexClass;
        this.vertices = new Object[] { v0, v1, v2, v3 };
    }

    public T getVertex(int index) {
        return BTRUtil.uncheckedCast(vertices[index]);
    }

    public void setVertex(int index, T value) {
        vertices[index] = value;
    }

    public static GraphicsQuad<PosTex> newPosTexQuad(PosTex v0, PosTex v1, PosTex v2, PosTex v3) {
        return new GraphicsQuad<>(PosTex.class, v0, v1, v2, v3);
    }

    public static GraphicsQuad<PosTex> newPosTexQuad() {
        return new GraphicsQuad<>(PosTex.class);
    }

    public static GraphicsQuad<PosXY> newPosXYQuad(PosXY v0, PosXY v1, PosXY v2, PosXY v3) {
        return new GraphicsQuad<>(PosXY.class, v0, v1, v2, v3);
    }

    @ToString
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PosTex extends VertexInfo {
        public final double x, y, z;
        public final float u, v;
    }

    @ToString
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PosXY extends VertexInfo {
        public final float x, y;
    }

    public static abstract class VertexInfo {}
}
