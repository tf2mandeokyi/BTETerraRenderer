package com.mndk.bteterrarenderer.mcconnector.client.gui.component;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

public interface GuiComponentCopy extends GuiEventListenerCopy {
    default void tick() {}
    boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden);
    void drawComponent(DrawContextWrapper<?> drawContextWrapper);
}
