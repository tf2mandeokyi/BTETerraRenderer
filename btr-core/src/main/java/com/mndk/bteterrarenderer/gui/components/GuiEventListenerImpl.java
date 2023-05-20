package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.minecraft.InputKey;

public interface GuiEventListenerImpl {
    default boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseReleased(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) { return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) { return false; }

    default boolean keyTyped(char typedChar, int keyCode) { return false; }
    default boolean keyPressed(InputKey key) { return false; }
}
