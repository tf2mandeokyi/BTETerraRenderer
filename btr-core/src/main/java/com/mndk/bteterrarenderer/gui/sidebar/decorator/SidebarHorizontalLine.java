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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GuiStaticConnector.INSTANCE.drawRect(0, 0, parent.elementWidth.get(), thickness, color);
    }

    @Override protected void init() {}
    @Override public void onWidthChange(int newWidth) {}
    @Override public void updateScreen() {}
    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) { return false; }
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
