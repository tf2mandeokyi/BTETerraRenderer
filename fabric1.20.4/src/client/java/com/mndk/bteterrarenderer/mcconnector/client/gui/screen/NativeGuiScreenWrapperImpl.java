package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nonnull;

public class NativeGuiScreenWrapperImpl extends NativeGuiScreenWrapper<Screen> {

    public NativeGuiScreenWrapperImpl(@Nonnull Screen delegate) {
        super(delegate);
    }

    public void onDisplayed() {}
    public void initGui(int width, int height) {
        getThisWrapped().init(MinecraftClient.getInstance(), width, height);
    }
    public void setScreenSize(int width, int height) {
        getThisWrapped().resize(MinecraftClient.getInstance(), width, height);
    }
    public void tick() {
        getThisWrapped().tick();
    }
    public void drawScreen(@Nonnull DrawContextWrapper<?> drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
        getThisWrapped().render(drawContextWrapper.get(), mouseX, mouseY, partialTicks);
    }
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return getThisWrapped().mouseClicked(mouseX, mouseY, mouseButton);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return getThisWrapped().mouseReleased(mouseX, mouseY, mouseButton);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        return getThisWrapped().mouseDragged(mouseX, mouseY, mouseButton, mouseX - pMouseX, mouseY - pMouseY);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return getThisWrapped().mouseScrolled(mouseX, mouseY, 0, scrollAmount);
    }
    public boolean charTyped(char typedChar, int keyCode) {
        return getThisWrapped().charTyped(typedChar, keyCode);
    }
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return getThisWrapped().keyPressed(key.glfwKeyCode, scanCode, modifiers);
    }
    public void onRemoved() {
        getThisWrapped().removed();
    }
    public boolean doesScreenPauseGame() {
        return getThisWrapped().shouldPause();
    }
    public boolean shouldCloseOnEsc() {
        return getThisWrapped().shouldCloseOnEsc();
    }
    public boolean alsoListensForKeyPress() {
        return true;
    }
}
