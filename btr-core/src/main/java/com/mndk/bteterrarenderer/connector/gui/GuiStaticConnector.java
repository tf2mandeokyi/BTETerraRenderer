package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public interface GuiStaticConnector {
    GuiStaticConnector INSTANCE = ImplFinder.search(GuiStaticConnector.class);

    void displayGuiScreen(AbstractGuiScreen gui);
    void drawRect(int x, int y, int w, int h, int color);
    void drawContinuousTexturedBox(IResourceLocation res,
                                   int x, int y, int u, int v,
                                   int width, int height,
                                   int textureWidth, int textureHeight,
                                   int topBorder, int bottomBorder, int leftBorder, int rightBorder,
                                   float zLevel);

}
