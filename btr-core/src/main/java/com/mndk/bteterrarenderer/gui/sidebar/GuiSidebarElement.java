package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.gui.components.GuiComponentImpl;

public abstract class GuiSidebarElement extends GuiComponentImpl {

    public GuiSidebar parent;
    public boolean hide;

    public GuiSidebarElement() {
        this.hide = false;
    }

    public final void initGui(GuiSidebar parent) {
        this.parent = parent;
        this.init();
    }

    public abstract int getHeight();

    protected abstract void init();

    public abstract void onWidthChange(int newWidth);

    public abstract void updateScreen();
    public abstract void drawComponent(double mouseX, double mouseY, float partialTicks);

    public abstract boolean mousePressed(double mouseX, double mouseY, int mouseButton);
    public abstract void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY);
    public abstract void mouseReleased(double mouseX, double mouseY, int state);

    public abstract boolean keyTyped(char typedChar, int keyCode);
}

