package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nonnull;

public class NativeGuiScreenWrapperImpl extends MinecraftObjectWrapper<Screen> implements NativeGuiScreenWrapper {

    public NativeGuiScreenWrapperImpl(@Nonnull Screen delegate) {
        super(delegate);
    }

    public void onDisplayed() {}
    public void initGui(int width, int height) {
        getWrapped().init(Minecraft.getInstance(), width, height);
    }
    public void setScreenSize(int width, int height) {
        getWrapped().resize(Minecraft.getInstance(), width, height);
    }
    public void tick() {
        getWrapped().tick();
    }
    public void drawScreen(@Nonnull DrawContextWrapper drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
        getWrapped().render(((DrawContextWrapperImpl) drawContextWrapper).getWrapped(), mouseX, mouseY, partialTicks);
    }
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return getWrapped().mouseClicked(mouseX, mouseY, mouseButton);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return getWrapped().mouseReleased(mouseX, mouseY, mouseButton);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        return getWrapped().mouseDragged(mouseX, mouseY, mouseButton, mouseX - pMouseX, mouseY - pMouseY);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return getWrapped().mouseScrolled(mouseX, mouseY, scrollAmount);
    }
    public boolean charTyped(char typedChar, int keyCode) {
        return getWrapped().charTyped(typedChar, keyCode);
    }
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return getWrapped().keyPressed(key.glfwKeyCode, scanCode, modifiers);
    }
    public void onRemoved() {
        getWrapped().removed();
    }
    public boolean doesScreenPauseGame() {
        return getWrapped().isPauseScreen();
    }
    public boolean shouldCloseOnEsc() {
        return getWrapped().shouldCloseOnEsc();
    }
    public boolean alsoListensForKeyPress() {
        return true;
    }
}
