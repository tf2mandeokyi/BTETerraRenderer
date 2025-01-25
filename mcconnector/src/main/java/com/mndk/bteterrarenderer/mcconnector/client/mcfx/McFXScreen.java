package com.mndk.bteterrarenderer.mcconnector.client.mcfx;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class McFXScreen<T extends McFXElement> extends AbstractGuiScreenCopy {

    @Getter(AccessLevel.PROTECTED)
    private final T mainComponent;
    private final boolean guiPausesGame, shouldCloseOnEsc;
    private int prevScreenWidth, prevScreenHeight;

    @Override
    protected void initGui() {
        this.mainComponent.init(this.getWidth());
    }

    @Override
    public void tick() {
        if (prevScreenWidth != this.getWidth() || prevScreenHeight != this.getHeight()) {
            this.mainComponent.onWidthChange(this.getWidth());
            this.prevScreenWidth = this.getWidth();
            this.prevScreenHeight = this.getHeight();
        }
        this.mainComponent.tick();
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks) {
        return this.mainComponent.mouseHovered(mouseX, mouseY, partialTicks, false);
    }

    @Override
    public void drawScreen(@Nonnull GuiDrawContextWrapper drawContextWrapper) {
        drawContextWrapper.pushMatrix();
        this.mainComponent.drawComponent(drawContextWrapper);
        drawContextWrapper.popMatrix();
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return this.mainComponent.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return this.mainComponent.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return this.mainComponent.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean charTyped(char key, int keyCode) {
        return this.mainComponent.charTyped(key, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return this.mainComponent.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        return this.mainComponent.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }

    @Override
    public void onRemoved() {}

    @Override
    public boolean doesScreenPauseGame() {
        return this.guiPausesGame;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.shouldCloseOnEsc;
    }

    @Override
    public boolean handleScreenEscape() {
        return this.mainComponent.handleScreenEscape();
    }

    @Override
    public boolean isChatFocused() {
        return false;
    }
}
