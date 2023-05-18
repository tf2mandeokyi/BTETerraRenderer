package com.mndk.bteterrarenderer.gui.sidebar.button;

import com.mndk.bteterrarenderer.gui.components.GuiButtonImpl;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

public class SidebarButton extends GuiSidebarElement {

    private GuiButtonImpl button;
    private final String buttonText;
    private final MouseClickedEvent event;

    public SidebarButton(String buttonText, MouseClickedEvent event) {
        this.buttonText = buttonText;
        this.event = event;
    }

    @Override
    protected void init() {
        this.button = new GuiButtonImpl(0, 0, parent.elementWidth.get(), 20, buttonText);
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.button.setWidth(newWidth);
    }

    public void setDisplayString(String newString) {
        this.button.setText(newString);
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void drawComponent(double mouseX, double mouseY, float partialTicks) {
        this.button.drawComponent(mouseX, mouseY, partialTicks);
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

    @Override public void updateScreen() {}
    @Override public boolean keyTyped(char typedChar, int keyCode) { return false; }
    @Override public void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int state) {}
}
