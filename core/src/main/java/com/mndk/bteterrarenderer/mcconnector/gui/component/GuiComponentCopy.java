package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public interface GuiComponentCopy extends GuiEventListenerCopy {
    default void tick() {}
    boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden);
    void drawComponent(DrawContextWrapper<?> drawContextWrapper);
}
