package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nonnull;

public record NativeGuiScreenWrapperImpl(@Nonnull Screen delegate) implements NativeGuiScreenWrapper {

    public void onDisplayed() {}
    public void initGui(int width, int height) {
        delegate.init(MinecraftClient.getInstance(), width, height);
    }
    public void setScreenSize(int width, int height) {
        delegate.resize(MinecraftClient.getInstance(), width, height);
    }
    public void tick() {
        delegate.tick();
    }
    public void drawScreen(@Nonnull GuiDrawContextWrapper context, int mouseX, int mouseY, float partialTicks) {
        delegate.render(((GuiDrawContextWrapperImpl) context).delegate, mouseX, mouseY, partialTicks);
    }
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return delegate.mouseClicked(mouseX, mouseY, mouseButton);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return delegate.mouseReleased(mouseX, mouseY, mouseButton);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        return delegate.mouseDragged(mouseX, mouseY, mouseButton, mouseX - pMouseX, mouseY - pMouseY);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return delegate.mouseScrolled(mouseX, mouseY, 0, scrollAmount);
    }
    public boolean charTyped(char typedChar, int keyCode) {
        return delegate.charTyped(typedChar, keyCode);
    }
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return delegate.keyPressed(key.glfwKeyCode, scanCode, modifiers);
    }
    public void onRemoved() {
        delegate.removed();
    }
    public boolean doesScreenPauseGame() {
        return delegate.shouldPause();
    }
    public boolean shouldCloseOnEsc() {
        return delegate.shouldCloseOnEsc();
    }
    public boolean alsoListensForKeyPress() {
        return true;
    }
}
