package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.BtrUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class GraphicsQuad<T extends GraphicsQuad.VertexInfo> {

    private final Object[] vertices;

    public GraphicsQuad() {
        this.vertices = new Object[4];
    }

    public GraphicsQuad(T v0, T v1, T v2, T v3) {
        this.vertices = new Object[] { v0, v1, v2, v3 };
    }

    public T getVertex(int index) {
        return BtrUtil.uncheckedCast(vertices[index]);
    }

    public void setVertex(int index, T value) {
        vertices[index] = value;
    }

    @ToString
    @RequiredArgsConstructor
    public static class PosTexColor extends VertexInfo {
        public final double x, y, z;
        public final float u, v, r, g, b, a;
    }

    @ToString
    @RequiredArgsConstructor
    public static class Pos extends VertexInfo {
        public final float x, y, z;
    }

    public static abstract class VertexInfo {}
}
