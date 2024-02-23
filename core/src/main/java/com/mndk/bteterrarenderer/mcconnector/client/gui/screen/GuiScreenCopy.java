package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.gui.component.GuiEventListenerCopy;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

import javax.annotation.Nonnull;

public interface GuiScreenCopy extends GuiEventListenerCopy {
    void initGui(int width, int height);
    void setScreenSize(int width, int height);

    void tick();
    void drawScreen(@Nonnull DrawContextWrapper<?> drawContextWrapper, int mouseX, int mouseY, float partialTicks);

    boolean mousePressed(double mouseX, double mouseY, int mouseButton);
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton);
    boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY);
    boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount);
    boolean charTyped(char typedChar, int keyCode);
    boolean keyPressed(InputKey key, int scanCode, int modifiers);

    void onRemoved();
    boolean doesScreenPauseGame();
    boolean shouldCloseOnEsc();
}
