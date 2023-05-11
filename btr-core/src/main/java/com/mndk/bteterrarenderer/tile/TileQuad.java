package com.mndk.bteterrarenderer.tile;

import lombok.RequiredArgsConstructor;

public class TileQuad {

    public final VertexInfo[] vertices;
    public final int glId;

    public TileQuad(int glId) {
        this.glId = glId;
        this.vertices = new VertexInfo[4];
    }

    public void setVertex(int index, VertexInfo vertexInfo) {
        this.vertices[index] = vertexInfo;
    }

    @RequiredArgsConstructor
    public static class VertexInfo {
        public final float x, y, z;
        public final float u, v, r, g, b, a;
    }
}
