package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;

import javax.annotation.Nonnull;

public abstract class AbstractBufferBuilderWrapper<T> extends MinecraftObjectWrapper<T> implements BufferBuilderWrapper {

    protected AbstractBufferBuilderWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    public final void nextPosTex(DrawContextWrapper drawContextWrapper, PosTex vertex, float alpha) {
        this.position(drawContextWrapper, vertex.pos)
                .texture(vertex.u, vertex.v)
                .color(1, 1, 1, alpha)
                .next();
    }

    public final void nextPosTexNorm(DrawContextWrapper drawContextWrapper, PosTexNorm vertex, float alpha) {
        this.position(drawContextWrapper, vertex.pos)
                .color(1, 1, 1, alpha)
                .texture(vertex.u, vertex.v)
                .light(0x00F000F0)
                .normal(drawContextWrapper, vertex.normal)
                .next();
    }
}
