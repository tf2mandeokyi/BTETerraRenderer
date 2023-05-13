package com.mndk.bteterrarenderer.gui.sidebar;

public abstract class GuiSidebarElement {

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
    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

    public abstract boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);
    public abstract void mouseReleased(int mouseX, int mouseY, int state);

    public abstract boolean keyTyped(char key, int keyCode);
}

