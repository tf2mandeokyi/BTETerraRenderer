package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public abstract class AbstractNativeTextureWrapper<T> extends MinecraftObjectWrapper<T> implements NativeTextureWrapper {
    private boolean deleted = false;
    public AbstractNativeTextureWrapper(@Nonnull T delegate) {
        super(delegate);
    }
    public void markAsDeleted() { this.deleted = true; }
}
