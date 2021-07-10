package com.mndk.bteterrarenderer.gui.sidebar;

import net.minecraft.client.gui.GuiButton;

public class SidebarButton extends GuiSidebarElement {



    private GuiButton button;
    private final String buttonText;
    private final MouseClickedEvent event;



    public SidebarButton(String buttonText, MouseClickedEvent event) {
        this.buttonText = buttonText;
        this.event = event;
    }


    @Override
    protected void init() {
        this.button = new GuiButton(-1, 0, 0, parent.elementWidth.get(), 20, buttonText);
    }



    @Override
    public void onWidthChange(int newWidth) {
        this.button.width = newWidth;
    }



    public void setDisplayString(String newString) {
        this.button.displayString = newString;
    }



    @Override
    public int getHeight() {
        return 20;
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.button.drawButton(parent.mc, mouseX, mouseY, partialTicks);
    }



    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(this.button.mousePressed(parent.mc, mouseX, mouseY)) {
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
    @Override public void keyTyped(char key, int keyCode) {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
