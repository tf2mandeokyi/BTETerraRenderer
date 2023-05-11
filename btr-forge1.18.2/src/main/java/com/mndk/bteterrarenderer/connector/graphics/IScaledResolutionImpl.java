package com.mndk.bteterrarenderer.connector.graphics;

import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;

@RequiredArgsConstructor @Getter
public class IScaledResolutionImpl implements IScaledResolution {
    private final int scaledWidth, scaledHeight;
    public IScaledResolutionImpl() {
        this(Minecraft.getInstance().getWindow());
    }
    public IScaledResolutionImpl(Window window) {
        this(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}
