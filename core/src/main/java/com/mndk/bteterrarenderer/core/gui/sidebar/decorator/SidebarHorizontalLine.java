package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;

public class SidebarHorizontalLine extends GuiSidebarElement {

    private final int thickness;
    private final int color;

    public SidebarHorizontalLine(int thickness, int color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public int getPhysicalHeight() {
        return thickness;
    }

    @Override
    public void drawComponent(Object poseStack) {
        RawGuiManager.fillRect(poseStack, 0, 0, this.getWidth(), thickness, color);
    }

    @Override protected void init() {}
    @Override public void onWidthChange() {}
}
