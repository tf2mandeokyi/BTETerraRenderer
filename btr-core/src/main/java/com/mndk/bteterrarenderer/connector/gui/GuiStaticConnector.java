package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.gui.components.GuiButtonImpl;

public interface GuiStaticConnector {
    GuiStaticConnector INSTANCE = ImplFinder.search();

    void displayGuiScreen(AbstractGuiScreen gui);
    void drawRect(int x, int y, int w, int h, int color);
    void drawButton(int x, int y, int width, int height, GuiButtonImpl.HoverState hoverState);
    void drawCheckBox(int x, int y, int width, int height, boolean checked);
    void drawTextFieldHighlight(int startX, int startY, int endX, int endY);
}
