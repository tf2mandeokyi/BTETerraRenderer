package com.mndk.bteterrarenderer.mcconnector.gui.components;

public abstract class GuiComponentCopy implements GuiEventListenerCopy {
    public void tick() {}
    public abstract void drawComponent(Object poseStack);
}
