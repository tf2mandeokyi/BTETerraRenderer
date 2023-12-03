package com.mndk.bteterrarenderer.core.gui.sidebar.wrapper;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class SidebarElementWrapper extends GuiSidebarElement {

    @Nullable
    private GuiSidebarElement delegate;

    public void setElement(GuiSidebarElement delegate) {
        this.delegate = delegate;
        if(delegate != null && this.getWidth() != -1) delegate.init(this.getWidth());
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
        if(delegate != null) delegate.init(this.getWidth());
    }

    @Override
    public void onWidthChange() {
        if(delegate != null) delegate.onWidthChange(this.getWidth());
    }

    @Override
    public int getCount() {
        return delegate != null ? delegate.getCount() : 0;
    }
}
