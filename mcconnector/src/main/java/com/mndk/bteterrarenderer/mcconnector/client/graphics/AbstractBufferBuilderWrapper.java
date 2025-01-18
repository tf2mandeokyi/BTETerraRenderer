package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;

import javax.annotation.Nonnull;

public abstract class AbstractBufferBuilderWrapper<T> extends MinecraftObjectWrapper<T> implements BufferBuilderWrapper {
    protected AbstractBufferBuilderWrapper(@Nonnull T delegate) {
        super(delegate);
    }
}
