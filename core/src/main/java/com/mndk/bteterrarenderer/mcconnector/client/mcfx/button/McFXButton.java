package com.mndk.bteterrarenderer.mcconnector.client.mcfx.button;

import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.ButtonWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

public class McFXButton extends McFXElement {

    private ButtonWidgetCopy button;
    private String buttonText;
    private final MouseClickedEvent event;

    public McFXButton(String buttonText, MouseClickedEvent event) {
        this.buttonText = buttonText;
        this.event = event;
    }

    @Override
    protected void init() {
        this.button = new ButtonWidgetCopy(0, 0, this.getWidth(), 20, buttonText);
    }

    @Override
    public void onWidthChange() {
        this.button.setWidth(this.getWidth());
    }

    public void setDisplayString(String newString) {
        this.buttonText = newString;
        this.button.setText(newString);
    }

    @Override
    public int getPhysicalHeight() {
        return 20;
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        return this.button.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        this.button.drawComponent(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (this.button.mousePressed(mouseX, mouseY, mouseButton)) {
            this.event.onMouseClicked(this, mouseButton);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface MouseClickedEvent {
        void onMouseClicked(McFXButton self, int mouseButton);
    }
}
