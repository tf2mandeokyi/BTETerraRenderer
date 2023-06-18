package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

public class SidebarBlank extends GuiSidebarElement {

    private final int height;

    public SidebarBlank(int height) {
        this.height = height;
    }

    @Override
    public int getPhysicalHeight() {
        return height;
    }

    @Override
    public int getVisualHeight() {
        return 0;
    }

    @Override
    protected void init() {}

    @Override
    public void onWidthChange(double newWidth) {}

    @Override
    public void drawComponent(Object poseStack) {}
}