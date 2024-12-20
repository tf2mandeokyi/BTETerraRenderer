package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;

import javax.annotation.Nonnull;

public abstract class BufferBuilderWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    protected BufferBuilderWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    /** PTCN stands for Position-Texture-Color-Normal */
    public abstract void beginPtcnTriangles();
    /** PTC stands for Position-Texture-Color */
    public abstract void beginPtcQuads();
    /** PTC stands for Position-Texture-Color */
    public abstract void beginPtcTriangles();
    /** PC stands for Position-Color */
    public abstract void beginPcQuads();
    /** PT stands for Position-Texture */
    public abstract void beginPtQuads();
    /** P stands for Position */
    public abstract void beginPQuads();

    /** PC stands for Position-Color */
    public final void pcNext(DrawContextWrapper<?> drawContextWrapper,
                             float x, float y, float z,
                             float r, float g, float b, float a) {
        this.position(drawContextWrapper, x, y, z)
                .color(r, g, b, a)
                .next();
    }

    /** PT stands for Position-Color */
    public final void ptNext(DrawContextWrapper<?> drawContextWrapper,
                             float x, float y, float z,
                             float u, float v) {
        this.position(drawContextWrapper, x, y, z)
                .texture(u, v)
                .next();
    }

    /** P stands for Position */
    public final void pNext(DrawContextWrapper<?> drawContextWrapper,
                            float x, float y, float z) {
        this.position(drawContextWrapper, x, y, z)
                .next();
    }

    public final BufferBuilderWrapper<T> position(DrawContextWrapper<?> drawContextWrapper, double x, double y, double z) {
        return this.position(drawContextWrapper, (float) x, (float) y, (float) z);
    }
    public final BufferBuilderWrapper<T> normal(DrawContextWrapper<?> drawContextWrapper, double x, double y, double z) {
        return this.normal(drawContextWrapper, (float) x, (float) y, (float) z);
    }

    public abstract BufferBuilderWrapper<T> position(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z);
    public abstract BufferBuilderWrapper<T> normal(DrawContextWrapper<?> drawContextWrapper, float x, float y, float z);
    public abstract BufferBuilderWrapper<T> texture(float u, float v);
    public abstract BufferBuilderWrapper<T> color(float r, float g, float b, float a);
    public abstract void next();

    public abstract void drawAndRender();

}
