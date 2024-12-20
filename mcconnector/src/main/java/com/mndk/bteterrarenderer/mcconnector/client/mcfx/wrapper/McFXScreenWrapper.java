package com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrappedScreen;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class McFXScreenWrapper extends McFXElement {

    @Nullable
    private AbstractGuiScreenCopy screen;
    private final Supplier<Integer> height;

    public McFXScreenWrapper setScreen(AbstractGuiScreenCopy screen) {
        // Replace old screen
        if (this.screen != null) {
            this.screen.onRemoved();
        }
        if (screen instanceof NativeGuiScreenWrappedScreen) {
            NativeGuiScreenWrappedScreen nativeWrapped = (NativeGuiScreenWrappedScreen) screen;
            nativeWrapped.getNativeScreen().onDisplayed();
        }

        this.screen = screen;
        if (screen == null || this.getWidth() == -1) return this;
        screen.initGui(this.getWidth(), this.height.get());
        return this;
    }

    public McFXScreenWrapper setHeight(int height) {
        if (screen == null) return this;
        this.screen.setScreenSize(this.getWidth(), height);
        return this;
    }

    public boolean isEmpty() {
        return this.screen == null;
    }

    @Override
    protected void init() {
        if (screen == null) return;
        screen.initGui(this.getWidth(), this.height.get());
    }

    @Override
    public void onWidthChange() {
        if (screen == null) return;
        screen.setScreenSize(this.getWidth(), this.height.get());
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        if (screen == null || mouseHidden) return false;
        return screen.mouseHovered(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        if (screen == null) return;
        screen.drawScreen(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (screen == null) return false;
        return screen.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (screen == null) return false;
        return screen.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if (screen == null) return false;
        return screen.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (screen == null) return false;
        return screen.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (screen == null) return false;
        return screen.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        if (screen == null) return false;
        return screen.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean handleScreenEscape() {
        if (screen == null) return true;

        boolean escapable = screen.handleScreenEscape();
        if (escapable) {
            this.screen.onRemoved();
            this.screen = null;
        }
        return false;
    }

    @Override
    public int getPhysicalHeight() {
        return this.height.get();
    }
}
