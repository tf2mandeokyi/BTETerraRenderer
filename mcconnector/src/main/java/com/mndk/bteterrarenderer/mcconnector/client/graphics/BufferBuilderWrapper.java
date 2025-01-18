package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;

public interface BufferBuilderWrapper {

    default BufferBuilderWrapper position(DrawContextWrapper drawContextWrapper, McCoord coord) {
        return this.position(drawContextWrapper, (float) coord.getX(), coord.getY(), (float) coord.getZ());
    }
    default BufferBuilderWrapper normal(DrawContextWrapper drawContextWrapper, McCoord coord) {
        return this.normal(drawContextWrapper, (float) coord.getX(), coord.getY(), (float) coord.getZ());
    }

    BufferBuilderWrapper position(DrawContextWrapper drawContextWrapper, float x, float y, float z);
    BufferBuilderWrapper normal(DrawContextWrapper drawContextWrapper, float x, float y, float z);
    BufferBuilderWrapper texture(float u, float v);
    BufferBuilderWrapper color(float r, float g, float b, float a);
    BufferBuilderWrapper light(int light);
    BufferBuilderWrapper defaultOverlay();
    void next();

    void drawAndRender();
}
