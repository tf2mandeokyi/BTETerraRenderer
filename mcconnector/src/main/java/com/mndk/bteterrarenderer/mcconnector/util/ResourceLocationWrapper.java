package com.mndk.bteterrarenderer.mcconnector.util;

import javax.annotation.Nonnull;

public class ResourceLocationWrapper<T> extends MinecraftNativeObjectWrapper<T> {
    protected ResourceLocationWrapper(@Nonnull T delegate) {
        super(delegate);
    }
}
