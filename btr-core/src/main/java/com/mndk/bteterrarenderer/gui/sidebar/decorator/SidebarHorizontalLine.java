package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

public class SidebarHorizontalLine extends GuiSidebarElement {

    private final int thickness;
    private final int color;

    public SidebarHorizontalLine(int thickness, int color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public int getHeight() {
        return thickness;
    }

    @Override
    public void drawComponent(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        GuiStaticConnector.INSTANCE.fillRect(poseStack, 0, 0, parent.elementWidth.get().intValue(), thickness, color);
    }

    @Override protected void init() {}
    @Override public void onWidthChange(double newWidth) {}
}
