package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import java.io.IOException;

@RequiredArgsConstructor
public class NativeGuiScreenWrapperImpl implements NativeGuiScreenWrapper {
    @Nonnull public final GuiScreen delegate;

    public void onDisplayed() {}
    public void initGui(int width, int height) {
        // This setWorldAndResolution calls initGui by itself, so there's no need to call it again
        delegate.setWorldAndResolution(Minecraft.getMinecraft(), width, height);
    }
    public void setScreenSize(int width, int height) {
        delegate.onResize(Minecraft.getMinecraft(), width, height);
    }
    public void tick() {
        delegate.updateScreen();
    }
    public void drawScreen(@Nonnull GuiDrawContextWrapper context, int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(mouseX, mouseY, partialTicks);
    }
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        try { delegate.handleMouseInput(); }
        catch (IOException ignored) {}
        return false;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        try { delegate.handleMouseInput(); }
        catch (IOException ignored) {}
        return false;
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        try { delegate.handleMouseInput(); }
        catch (IOException ignored) {}
        return false;
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        try { delegate.handleMouseInput(); }
        catch (IOException ignored) {}
        return false;
    }
    public boolean charTyped(char typedChar, int keyCode) {
        try { delegate.handleKeyboardInput(); }
        catch (IOException ignored) {}
        return false;
    }
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return false;
    }
    public void onRemoved() {
        delegate.onGuiClosed();
    }
    public boolean doesScreenPauseGame() {
        return delegate.doesGuiPauseGame();
    }
    public boolean shouldCloseOnEsc() {
        return true;
    }
    public boolean alsoListensForKeyPress() {
        return false;
    }
}
