package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;

import javax.annotation.Nonnull;

public abstract class BufferBuilderWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    protected BufferBuilderWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    /** PTC stands for Position-Texture-Color */
    public abstract void beginPTCQuads();
    /** PTC stands for Position-Texture-Color */
    public abstract void beginPTCTriangles();
    /** PTC stands for Position-Texture-Color */
    public abstract void ptc(DrawContextWrapper<?> drawContextWrapper,
                             float x, float y, float z,
                             float u, float v,
                             float r, float g, float b, float a);

    /** PC stands for Position-Color */
    public abstract void beginPCQuads();
    /** PC stands for Position-Color */
    public abstract void pc(DrawContextWrapper<?> drawContextWrapper,
                            float x, float y, float z,
                            float r, float g, float b, float a);

    /** PT stands for Position-Texture */
    public abstract void beginPTQuads();
    /** PT stands for Position-Color */
    public abstract void pt(DrawContextWrapper<?> drawContextWrapper,
                            float x, float y, float z,
                            float u, float v);

    /** P stands for Position */
    public abstract void beginPQuads();
    /** P stands for Position */
    public abstract void p(DrawContextWrapper<?> drawContextWrapper,
                           float x, float y, float z);

    public abstract void drawAndRender();

}
