package com.mndk.bteterrarenderer.gui.sidebar.elem;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

import java.io.IOException;

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
    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException { return false; }
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) { }
    @Override public void mouseReleased(int mouseX, int mouseY, int state) { }
    @Override public void keyTyped(char key, int keyCode) throws IOException { }
}
