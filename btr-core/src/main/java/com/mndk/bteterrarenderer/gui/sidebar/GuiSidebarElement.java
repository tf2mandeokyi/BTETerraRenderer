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

    public abstract void onWidthChange(double newWidth);

    public void updateScreen() {}
}

