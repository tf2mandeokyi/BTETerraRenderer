package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

public class SidebarHorizontalLine extends GuiSidebarElement {

    private final int thickness;
    private final int color;

    public SidebarHorizontalLine(int thickness, int color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public int getHeight() {
        return thickness;
    }

    @Override
    public void drawComponent(double mouseX, double mouseY, float partialTicks) {
        GuiStaticConnector.INSTANCE.drawRect(0, 0, parent.elementWidth.get(), thickness, color);
    }

    @Override protected void init() {}
    @Override public void onWidthChange(int newWidth) {}
    @Override public void updateScreen() {}
    @Override public boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    @Override public boolean keyTyped(char typedChar, int keyCode) { return false; }
    @Override public void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int state) {}
}
