package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public abstract class GuiComponentCopy implements GuiEventListenerCopy {
    public void tick() {}
    public abstract void drawComponent(DrawContextWrapper drawContextWrapper);
}
