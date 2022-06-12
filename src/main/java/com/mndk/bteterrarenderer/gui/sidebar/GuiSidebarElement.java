package com.mndk.bteterrarenderer.gui.sidebar;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public abstract class GuiSidebarElement extends Gui {

    public GuiSidebar parent;
    protected FontRenderer fontRenderer;
    public boolean hide;

    public GuiSidebarElement() {
        this.hide = false;
    }

    public final void initGui(GuiSidebar parent, FontRenderer renderer) {
        this.parent = parent;
        this.fontRenderer = renderer;
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

