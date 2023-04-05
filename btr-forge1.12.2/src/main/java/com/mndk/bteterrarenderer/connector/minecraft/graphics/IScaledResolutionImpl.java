package com.mndk.bteterrarenderer.connector.minecraft.graphics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.ScaledResolution;

@RequiredArgsConstructor @Getter
public class IScaledResolutionImpl implements IScaledResolution {
    private final int scaledWidth, scaledHeight;
    public IScaledResolutionImpl(ScaledResolution scaledResolution) {
        this(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
    }
}
