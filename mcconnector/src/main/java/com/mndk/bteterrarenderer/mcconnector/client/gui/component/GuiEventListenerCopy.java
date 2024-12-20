package com.mndk.bteterrarenderer.mcconnector.client.gui.component;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;

public interface GuiEventListenerCopy {
    int HOVERED_COLOR = 0xFFFFFFA0;
    int NORMAL_BORDER_COLOR = 0xFFA0A0A0;
    int FOCUSED_BORDER_COLOR = 0xFFFFFFFF;

    int ERROR_TEXT_COLOR = 0xFFFF0000;
    int DISABLED_TEXT_COLOR = 0xFF707070;
    int NORMAL_TEXT_COLOR = 0xFFD8D8D8;

    int NULL_COLOR = 0;

    default boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseReleased(double mouseX, double mouseY, int mouseButton) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) { return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) { return false; }
    default boolean charTyped(char typedChar, int keyCode) { return false; }
    default boolean keyPressed(InputKey key, int scanCode, int modifiers) { return false; }

    /**
     * @return {@code false} if it's not okay to escape the current screen when ESC is pressed.
     *         {@code true} otherwise.
     */
    default boolean handleScreenEscape() { return true; }

    default FontWrapper<?> getDefaultFont() {
        return McConnector.client().getDefaultFont();
    }
}
