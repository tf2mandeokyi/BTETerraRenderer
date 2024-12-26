package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class NativeGuiScreenWrappedScreen extends AbstractGuiScreenCopy {

    @Nonnull @Getter
    private final NativeGuiScreenWrapper nativeScreen;
    private final boolean ignoreFirstKeyInput;
    private boolean initialized = false;
    private boolean charTyped = false, keyPressed = false;
    private int mouseX, mouseY;
    private float mousePartialTicks;
    
    @Override
    protected void initGui() {
        nativeScreen.initGui(this.getWidth(), this.getHeight());
        initialized = true;
    }
    
    @Override
    public void setScreenSize(int width, int height) {
        super.setScreenSize(width, height);
        if (initialized) nativeScreen.setScreenSize(width, height);
    }
    
    @Override
    public void tick() {
        nativeScreen.tick();
    }
    
    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mousePartialTicks = partialTicks;
        return false;
    }
    
    @Override
    public void drawScreen(@Nonnull DrawContextWrapper drawContextWrapper) {
        // TODO: Fix tooltips not being rendered properly
        // TODO: Fix text highlight not being translated
        nativeScreen.drawScreen(drawContextWrapper, this.mouseX, this.mouseY, this.mousePartialTicks);
    }
    
    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        // TODO: Fix lwjgl Mouse not being translated
        return nativeScreen.mousePressed(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        // TODO: Fix lwjgl Mouse not being translated
        return nativeScreen.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        // TODO: Fix lwjgl Mouse not being translated
        return nativeScreen.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        // TODO: Fix lwjgl Mouse not being translated
        return nativeScreen.mouseScrolled(mouseX, mouseY, scrollAmount);
    }
    
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (this.ignoreFirstKeyInput && !this.charTyped) {
            this.charTyped = true;
            return false;
        }
        return nativeScreen.charTyped(typedChar, keyCode);
    }
    
    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        if (this.ignoreFirstKeyInput && !this.keyPressed) {
            this.keyPressed = true;
            if (!nativeScreen.alsoListensForKeyPress()) {
                this.charTyped = true;
            }
            return false;
        }
        return nativeScreen.keyPressed(key, scanCode, modifiers);
    }
    
    @Override
    public void onRemoved() {
        nativeScreen.onRemoved();
    }
    
    @Override
    public boolean doesScreenPauseGame() {
        return nativeScreen.doesScreenPauseGame();
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return nativeScreen.shouldCloseOnEsc();
    }

    @Override
    public boolean isChatFocused() {
        return false;
    }
}
