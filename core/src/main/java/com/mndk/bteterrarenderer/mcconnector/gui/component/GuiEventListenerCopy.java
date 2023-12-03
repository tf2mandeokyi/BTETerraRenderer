package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.input.InputKey;

public interface GuiEventListenerCopy {
    int HOVERED_COLOR = 0xFFFFFFA0;
    int NORMAL_BORDER_COLOR = 0xFFA0A0A0;
    int FOCUSED_BORDER_COLOR = 0xFFFFFFFF;

    int ERROR_TEXT_COLOR = 0xFFFF0000;
    int DISABLED_TEXT_COLOR = 0xFF707070;
    int NORMAL_TEXT_COLOR = 0xFFD8D8D8;

    int NULL_COLOR = 0;

    /**
     * This should be called before the {@link GuiComponentCopy#drawComponent} call.
     * @return {@code true} if the component has reacted the mouse hover. {@code false} otherwise.
     */
    default boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) { return false; }
    default boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseReleased(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) { return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) { return false; }

    default boolean keyTyped(char typedChar, int keyCode) { return false; }
    default boolean keyPressed(InputKey key) { return false; }
}
