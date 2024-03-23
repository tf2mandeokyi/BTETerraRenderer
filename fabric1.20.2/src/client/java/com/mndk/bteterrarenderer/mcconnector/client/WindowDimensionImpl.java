package com.mndk.bteterrarenderer.mcconnector.client;

import net.minecraft.client.util.Window;

public record WindowDimensionImpl(Window window) implements WindowDimension {
    public int getPixelWidth() { return this.window.getFramebufferWidth(); }
    public int getPixelHeight() { return this.window.getFramebufferHeight(); }
    public int getScaledWidth() { return this.window.getScaledWidth(); }
    public int getScaledHeight() { return this.window.getScaledHeight(); }
}
