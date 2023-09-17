package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.gui.components.GuiComponentCopy;

public abstract class GuiSidebarElement extends GuiComponentCopy {

    /**
     * This is initialized during {@link GuiSidebarElement#initComponent} call,
     * so there's no need to initialize this in the constructor.
     */
    public GuiSidebar parent;
    public boolean hide = false;

    public final void initComponent(GuiSidebar parent) {
        this.parent = parent;
        this.init();
    }

    public abstract int getPhysicalHeight();
    public int getVisualHeight() {
        return this.getPhysicalHeight();
    }

    protected abstract void init();

    public abstract void onWidthChange(double newWidth);
}

