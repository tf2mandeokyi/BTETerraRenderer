package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IScaledScreenSize;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;

@RequiredArgsConstructor @Getter
public class IScaledScreenSizeImpl implements IScaledScreenSize {
    private final int width, height;
    public IScaledScreenSizeImpl() {
        this(Minecraft.getInstance().getWindow());
    }
    public IScaledScreenSizeImpl(Window window) {
        this(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}
