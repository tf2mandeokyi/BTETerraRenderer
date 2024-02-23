package com.mndk.bteterrarenderer.mcconnector.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class WindowDimensionImpl implements WindowDimension {
    public int getPixelWidth() { return Minecraft.getMinecraft().displayWidth; }
    public int getPixelHeight() { return Minecraft.getMinecraft().displayHeight; }
    public int getScaledWidth() { return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(); }
    public int getScaledHeight() { return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(); }
}
