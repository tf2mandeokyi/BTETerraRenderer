package com.mndk.bteterrarenderer.connector.graphics;

import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;

@RequiredArgsConstructor @Getter
public class IScaledScreenSizeImpl18 implements IScaledScreenSize {
    private final int width, height;
    public IScaledScreenSizeImpl18() {
        this(Minecraft.getInstance().getWindow());
    }
    public IScaledScreenSizeImpl18(Window window) {
        this(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}
