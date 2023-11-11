package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.util.input.InputKey;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class GuiSidebarElementWrapper extends GuiSidebarElement {

    @Nullable
    private GuiSidebarElement delegate;

    public void setElement(GuiSidebarElement delegate) {
        this.delegate = delegate;
        if(this.parent != null && delegate != null) delegate.initComponent(this.parent);
    }

    @Override
    public void drawComponent(Object poseStack) {
        if(delegate != null) delegate.drawComponent(poseStack);
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        return delegate != null && delegate.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return delegate != null && delegate.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return delegate != null && delegate.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        return delegate != null && delegate.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return delegate != null && delegate.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        return delegate != null && delegate.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key) {
        return delegate != null && delegate.keyPressed(key);
    }

    @Override
    public int getPhysicalHeight() {
        return delegate != null ? delegate.getPhysicalHeight() : 0;
    }

    @Override
    protected void init() {
        if(delegate != null) delegate.initComponent(this.parent);
    }

    @Override
    public void onWidthChange(double newWidth) {
        if(delegate != null) delegate.onWidthChange(newWidth);
    }
}
