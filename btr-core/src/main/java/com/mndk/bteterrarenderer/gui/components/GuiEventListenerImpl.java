package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.minecraft.InputKey;

public interface GuiEventListenerImpl {
    default boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    default void mouseReleased(double mouseX, double mouseY, int mouseButton) {}
    default void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {}

    default boolean keyTyped(char typedChar, int keyCode) { return false; }
    default boolean keyPressed(InputKey key) { return false; }
}
