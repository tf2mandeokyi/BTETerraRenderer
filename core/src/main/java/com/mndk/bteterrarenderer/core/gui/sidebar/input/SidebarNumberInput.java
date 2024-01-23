package com.mndk.bteterrarenderer.core.gui.sidebar.input;

import com.mndk.bteterrarenderer.core.gui.component.GuiNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public class SidebarNumberInput extends GuiSidebarElement {

    private final PropertyAccessor<Double> value;
    private final String text;
    private GuiNumberInput textField;

    public SidebarNumberInput(PropertyAccessor<Double> value, String text) {
        this.value = value;
        this.text = text;
    }

    @Override
    protected void init() {
        this.textField = new GuiNumberInput(
                0, 0, this.getWidth(), 20,
                this.value, this.text
        );
    }

    @Override
    public void tick() {
        this.textField.tick();
    }

    @Override
    public void onWidthChange() {
        this.textField.setWidth(this.getWidth());
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        return textField.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        textField.drawComponent(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
       textField.mousePressed(mouseX, mouseY, mouseButton);
       return false;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        return textField.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key) {
        return textField.keyPressed(key);
    }

    @Override public int getPhysicalHeight() { return 20; }
}
