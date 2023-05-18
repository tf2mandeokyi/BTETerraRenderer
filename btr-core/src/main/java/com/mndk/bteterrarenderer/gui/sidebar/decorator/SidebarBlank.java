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
    @Override public void drawComponent(double mouseX, double mouseY, float partialTicks) { }
    @Override public boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    @Override public void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) { }
    @Override public void mouseReleased(double mouseX, double mouseY, int state) { }
    @Override public boolean keyTyped(char typedChar, int keyCode) { return false; }
}
