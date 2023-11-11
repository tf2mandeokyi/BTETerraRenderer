package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IScaledScreenSize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

@RequiredArgsConstructor @Getter
public class IScaledScreenSizeImpl implements IScaledScreenSize {
    private final int width, height;
    public IScaledScreenSizeImpl() {
        this(MinecraftClient.getInstance().getWindow());
    }
    public IScaledScreenSizeImpl(Window window) {
        this(window.getScaledWidth(), window.getScaledHeight());
    }
}
