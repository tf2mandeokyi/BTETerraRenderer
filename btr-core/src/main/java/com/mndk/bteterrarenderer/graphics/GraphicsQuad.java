package com.mndk.bteterrarenderer.graphics;

import com.mndk.bteterrarenderer.util.BtrUtil;
import lombok.RequiredArgsConstructor;

public class GraphicsQuad<T extends GraphicsQuad.VertexInfo> {

    private final T[] vertices;

    public GraphicsQuad() {
        this.vertices = BtrUtil.uncheckedCast(new Object[4]);
    }

    public GraphicsQuad(T v0, T v1, T v2, T v3) {
        this.vertices = BtrUtil.uncheckedCast(new Object[] { v0, v1, v2, v3 });
    }

    public T getVertex(int index) {
        return BtrUtil.uncheckedCast(vertices[index]);
    }

    public void setVertex(int index, T value) {
        vertices[index] = value;
    }

    @RequiredArgsConstructor
    public static class PosTexColor extends VertexInfo {
        public final double x, y, z;
        public final float u, v, r, g, b, a;
    }

    @RequiredArgsConstructor
    public static class Pos extends VertexInfo {
        public final float x, y, z;
    }

    public static class VertexInfo {}
}
