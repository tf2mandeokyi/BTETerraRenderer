package com.mndk.bteterrarenderer.connector.graphics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

@RequiredArgsConstructor @Getter
public class IScaledResolutionImpl implements IScaledResolution {
    private final int scaledWidth, scaledHeight;
    public IScaledResolutionImpl() {
        this(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public IScaledResolutionImpl(ScaledResolution scaledResolution) {
        this(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
    }
}
