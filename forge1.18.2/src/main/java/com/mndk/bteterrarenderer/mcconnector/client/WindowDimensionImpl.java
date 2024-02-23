package com.mndk.bteterrarenderer.mcconnector.client;

import com.mojang.blaze3d.platform.Window;

public record WindowDimensionImpl(Window window) implements WindowDimension {
    public int getPixelWidth() { return window.getWidth(); }
    public int getPixelHeight() { return window.getHeight(); }
    public int getScaledWidth() { return window.getGuiScaledWidth(); }
    public int getScaledHeight() { return window.getGuiScaledHeight(); }
}
