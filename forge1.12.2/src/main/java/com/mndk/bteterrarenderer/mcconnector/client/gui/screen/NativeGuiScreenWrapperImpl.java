package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import java.io.IOException;

public class NativeGuiScreenWrapperImpl extends MinecraftObjectWrapper<GuiScreen> implements NativeGuiScreenWrapper {
    public NativeGuiScreenWrapperImpl(@Nonnull GuiScreen delegate) {
        super(delegate);
    }

    public void onDisplayed() {}
    public void initGui(int width, int height) {
        // This setWorldAndResolution calls initGui by itself, so there's no need to call it again
        getWrapped().setWorldAndResolution(Minecraft.getMinecraft(), width, height);
    }
    public void setScreenSize(int width, int height) {
        getWrapped().onResize(Minecraft.getMinecraft(), width, height);
    }
    public void tick() {
        getWrapped().updateScreen();
    }
    public void drawScreen(@Nonnull DrawContextWrapper drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
        getWrapped().drawScreen(mouseX, mouseY, partialTicks);
    }
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        try { getWrapped().handleMouseInput(); } catch (IOException ignored) {}
        return false;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        try { getWrapped().handleMouseInput(); } catch (IOException ignored) {}
        return false;
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        try { getWrapped().handleMouseInput(); } catch (IOException ignored) {}
        return false;
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        try { getWrapped().handleMouseInput(); } catch (IOException ignored) {}
        return false;
    }
    public boolean charTyped(char typedChar, int keyCode) {
        try { getWrapped().handleKeyboardInput(); } catch (IOException ignored) {}
        return false;
    }
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return false;
    }
    public void onRemoved() {
        getWrapped().onGuiClosed();
    }
    public boolean doesScreenPauseGame() {
        return getWrapped().doesGuiPauseGame();
    }
    public boolean shouldCloseOnEsc() {
        return true;
    }
    public boolean alsoListensForKeyPress() {
        return false;
    }
}
