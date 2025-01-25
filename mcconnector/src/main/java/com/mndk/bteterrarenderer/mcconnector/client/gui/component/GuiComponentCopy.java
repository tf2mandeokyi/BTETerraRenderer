package com.mndk.bteterrarenderer.mcconnector.client.gui.component;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;

public interface GuiComponentCopy extends GuiEventListenerCopy {
    default void tick() {}
    boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden);
    void drawComponent(GuiDrawContextWrapper drawContextWrapper);
}
