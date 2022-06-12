package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

public class SidebarBlank extends GuiSidebarElement {

    private final int height;

    public SidebarBlank(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override protected void init() {}
    @Override public void onWidthChange(int newWidth) {}
    @Override public void updateScreen() {}
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) { }
    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) { return false; }
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) { }
    @Override public void mouseReleased(int mouseX, int mouseY, int state) { }
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
}
