package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;

import javax.annotation.Nonnull;

public abstract class BufferBuilderWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    protected BufferBuilderWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    public void nextPosTex(DrawContextWrapper<?> drawContextWrapper, PosTex vertex, float alpha) {
        this.position(drawContextWrapper, vertex.pos)
                .texture(vertex.u, vertex.v)
                .color(1, 1, 1, alpha)
                .next();
    }

    public void nextPosTex(DrawContextWrapper<?> drawContextWrapper, PosTexNorm vertex, float alpha) {
        this.position(drawContextWrapper, vertex.pos)
                .texture(vertex.u, vertex.v)
                .color(1, 1, 1, alpha)
                .next();
    }

    public final BufferBuilderWrapper<T> position(DrawContextWrapper<?> drawContextWrapper, McCoord coord) {
        return this.position(drawContextWrapper, (float) coord.getX(), coord.getY(), (float) coord.getZ());
    }
    public final BufferBuilderWrapper<T> normal(DrawContextWrapper<?> drawContextWrapper, McCoord coord) {
        return this.normal(drawContextWrapper, (float) coord.getX(), coord.getY(), (float) coord.getZ());
    }

    public abstract BufferBuilderWrapper<T> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z);
    public abstract BufferBuilderWrapper<T> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z);
    public abstract BufferBuilderWrapper<T> texture(float u, float v);
    public abstract BufferBuilderWrapper<T> color(float r, float g, float b, float a);
    public abstract BufferBuilderWrapper<T> light(int light);
    public abstract BufferBuilderWrapper<T> defaultOverlay();
    public abstract void next();

    public abstract void drawAndRender();

}
