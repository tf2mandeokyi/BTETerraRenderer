package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

@ConnectorImpl
public class GuiStaticConnectorImpl implements GuiStaticConnector {
    public void displayGuiScreen(Object gui) {
        Minecraft.getMinecraft().displayGuiScreen((GuiScreen) gui);
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, w, h, color);
    }

    public void drawContinuousTexturedBox(IResourceLocation res,
                                          int x, int y, int u, int v,
                                          int width, int height,
                                          int textureWidth, int textureHeight,
                                          int topBorder, int bottomBorder, int leftBorder, int rightBorder,
                                          float zLevel) {

        ResourceLocation resourceLocation = ((IResourceLocationImpl) res).getResourceLocation();
        GuiUtils.drawContinuousTexturedBox(
                resourceLocation,
                x, y, u, v,
                width, height, textureWidth, textureHeight,
                topBorder, bottomBorder, leftBorder, rightBorder,
                zLevel
        );
    }

}
