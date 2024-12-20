package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class NativeTextureWrapper extends MinecraftNativeObjectWrapper<Object> {
    private boolean deleted = false;
    public NativeTextureWrapper(@Nonnull Object delegate) {
        super(delegate);
    }
    public void markAsDeleted() { this.deleted = true; }
}
