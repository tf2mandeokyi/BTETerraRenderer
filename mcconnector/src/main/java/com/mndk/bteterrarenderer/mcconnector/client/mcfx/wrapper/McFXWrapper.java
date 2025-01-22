package com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
public class McFXWrapper extends McFXElement {

    @Nullable
    private McFXElement element;

    @Setter
    private int topPadding = 0, leftPadding = 0, bottomPadding = 0, rightPadding = 0;

    public McFXWrapper(@Nullable McFXElement element) {
        this.element = element;
    }

    @SuppressWarnings("UnusedReturnValue")
    public McFXWrapper setElement(McFXElement element) {
        this.element = element;
        if (element == null || this.getWidth() == -1) return this;
        element.init(this.getWidth() - leftPadding - rightPadding);
        return this;
    }

    public McFXWrapper setPadding(int top, int left, int bottom, int right) {
        this.topPadding = top;
        this.leftPadding = left;
        this.bottomPadding = bottom;
        this.rightPadding = right;
        return this;
    }

    @Override
    protected void init() {
        if (element == null) return;
        element.init(this.getWidth() - leftPadding - rightPadding);
    }

    @Override
    public void onWidthChange() {
        if (element == null) return;
        element.onWidthChange(this.getWidth() - leftPadding - rightPadding);
    }

    @Override
    public void tick() {
        if (element == null) return;
        element.tick();
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        if (element == null) return false;
        return element.mouseHovered(mouseX - leftPadding, mouseY - topPadding, partialTicks, mouseHidden);
    }

    @Override
    public void drawElement(GuiDrawContextWrapper drawContextWrapper) {
        drawContextWrapper.translate(leftPadding, topPadding, 0);
        if (element != null) element.drawComponent(drawContextWrapper);
        drawContextWrapper.translate(-leftPadding, -topPadding, 0);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (element == null) return false;
        return element.mousePressed(mouseX - leftPadding, mouseY - topPadding, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (element == null) return false;
        return element.mouseReleased(mouseX - leftPadding, mouseY - topPadding, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if (element == null) return false;
        return element.mouseDragged(mouseX - leftPadding, mouseY - topPadding, mouseButton, pMouseX - leftPadding, pMouseY - topPadding);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (element == null) return false;
        return element.mouseScrolled(mouseX - leftPadding, mouseY - topPadding, scrollAmount);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (element == null) return false;
        return element.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        if (element == null) return false;
        return element.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean handleScreenEscape() {
        if (element == null) return true;
        return element.handleScreenEscape();
    }

    @Override
    public int getPhysicalHeight() {
        return (element != null ? element.getPhysicalHeight() : 0) + this.topPadding + this.bottomPadding;
    }

    @Override
    public int getVisualHeight() {
        return (element != null ? element.getVisualHeight() : 0) + this.topPadding;
    }

    @Override
    public int getCount() {
        return element != null ? element.getCount() : 0;
    }
}
