package com.mndk.bteterrarenderer.core.gui.sidebar.wrapper;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.*;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SidebarElementWrapper extends GuiSidebarElement {

    @Nullable
    private GuiSidebarElement delegate;

    @Setter
    private int topPadding = 0, leftPadding = 0, bottomPadding = 0, rightPadding = 0;

    public void setElement(GuiSidebarElement delegate) {
        this.delegate = delegate;
        if(delegate == null || this.getWidth() == -1) return;
        delegate.init(this.getWidth() - leftPadding - rightPadding);
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        drawContextWrapper.translate(leftPadding, topPadding, 0);
        if(delegate != null) delegate.drawComponent(drawContextWrapper);
        drawContextWrapper.translate(-leftPadding, -topPadding, 0);
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        if(delegate == null) return false;
        return delegate.mouseHovered(mouseX - leftPadding, mouseY - topPadding, partialTicks, mouseHidden);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(delegate == null) return false;
        return delegate.mousePressed(mouseX - leftPadding, mouseY - topPadding, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if(delegate == null) return false;
        return delegate.mouseReleased(mouseX - leftPadding, mouseY - topPadding, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(delegate == null) return false;
        return delegate.mouseDragged(mouseX - leftPadding, mouseY - topPadding, mouseButton, pMouseX - leftPadding, pMouseY - topPadding);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if(delegate == null) return false;
        return delegate.mouseScrolled(mouseX - leftPadding, mouseY - topPadding, scrollAmount);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if(delegate == null) return false;
        return delegate.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key) {
        if(delegate == null) return false;
        return delegate.keyPressed(key);
    }

    @Override
    public int getPhysicalHeight() {
        return (delegate != null ? delegate.getPhysicalHeight() : 0) + this.topPadding + this.bottomPadding;
    }

    @Override
    public int getVisualHeight() {
        return (delegate != null ? delegate.getVisualHeight() : 0) + this.topPadding;
    }

    @Override
    protected void init() {
        if(delegate == null) return;
        delegate.init(this.getWidth() - leftPadding - rightPadding);
    }

    @Override
    public void onWidthChange() {
        if(delegate == null) return;
        delegate.onWidthChange(this.getWidth() - leftPadding - rightPadding);
    }

    @Override
    public int getCount() {
        return delegate != null ? delegate.getCount() : 0;
    }
}
