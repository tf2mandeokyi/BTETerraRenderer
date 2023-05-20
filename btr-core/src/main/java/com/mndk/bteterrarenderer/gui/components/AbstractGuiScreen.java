package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSize;
import lombok.Setter;

import java.util.function.Supplier;

@Setter
public abstract class AbstractGuiScreen implements GuiEventListenerImpl {
    public Supplier<Integer> guiWidth;
    public Supplier<IScaledScreenSize> screenSize;

    public abstract void initGui();
    public abstract void drawScreen(Object poseStack, double mouseX, double mouseY, float partialTicks);
    public abstract void updateScreen();
    public abstract boolean mousePressed(double mouseX, double mouseY, int mouseButton);
    public abstract boolean mouseReleased(double mouseX, double mouseY, int mouseButton);
    public abstract boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY);
    public abstract boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount);
    public abstract boolean keyTyped(char key, int keyCode);

    public abstract void onClose();
    public abstract boolean doesScreenPauseGame();
}
