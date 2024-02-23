package com.mndk.bteterrarenderer.mcconnector.client;

import net.minecraft.client.util.Window;

public record WindowDimensionImpl(Window window) implements WindowDimension {
    public int getPixelWidth() { return this.window.getWidth(); }
    public int getPixelHeight() { return this.window.getHeight(); }
    public int getScaledWidth() { return this.window.getScaledWidth(); }
    public int getScaledHeight() { return this.window.getScaledHeight(); }
}
