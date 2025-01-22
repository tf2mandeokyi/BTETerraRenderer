package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.Getter;

@Getter
public abstract class AbstractNativeTextureWrapper implements NativeTextureWrapper {
    private boolean deleted = false;
    public void markAsDeleted() { this.deleted = true; }
}
