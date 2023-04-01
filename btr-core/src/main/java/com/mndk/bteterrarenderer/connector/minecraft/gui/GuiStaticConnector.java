package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.minecraft.ResourceLocationConnector;

public interface GuiStaticConnector {

    void displayGuiScreen(Object gui);
    void drawRect(int x, int y, int w, int h, int color);
    void drawContinuousTexturedBox(ResourceLocationConnector res,
                                   int x, int y, int u, int v,
                                   int width, int height,
                                   int textureWidth, int textureHeight,
                                   int topBorder, int bottomBorder, int leftBorder, int rightBorder,
                                   float zLevel);

}
