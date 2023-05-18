package com.mndk.bteterrarenderer.gui.components;

public abstract class GuiComponentImpl implements GuiEventListenerImpl {
    public float zLevel = 0;

    public abstract void drawComponent(double mouseX, double mouseY, float partialTicks);
}
