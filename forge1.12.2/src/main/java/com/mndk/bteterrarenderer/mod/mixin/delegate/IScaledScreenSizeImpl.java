package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IScaledScreenSize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

@RequiredArgsConstructor @Getter
public class IScaledScreenSizeImpl implements IScaledScreenSize {
    private final int width, height;
    public IScaledScreenSizeImpl() {
        this(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public IScaledScreenSizeImpl(ScaledResolution scaledResolution) {
        this(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
    }
}
