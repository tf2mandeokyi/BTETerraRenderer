package com.mndk.bteterrarenderer.core.gui.sidebar.button;

import com.mndk.bteterrarenderer.core.gui.components.GuiButtonCopy;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;

public class SidebarButton extends GuiSidebarElement {

    private GuiButtonCopy button;
    private final String buttonText;
    private final MouseClickedEvent event;

    public SidebarButton(String buttonText, MouseClickedEvent event) {
        this.buttonText = buttonText;
        this.event = event;
    }

    @Override
    protected void init() {
        this.button = new GuiButtonCopy(0, 0, this.getWidth(), 20, buttonText);
    }

    @Override
    public void onWidthChange() {
        this.button.setWidth(this.getWidth());
    }

    public void setDisplayString(String newString) {
        this.button.setText(newString);
    }

    @Override
    public int getPhysicalHeight() {
        return 20;
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        return this.button.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawComponent(Object poseStack) {
        this.button.drawComponent(poseStack);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.button.mousePressed(mouseX, mouseY, mouseButton)) {
            this.event.onMouseClicked(this, mouseButton);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface MouseClickedEvent {
        void onMouseClicked(SidebarButton self, int mouseButton);
    }
}
