package com.mndk.bteterrarenderer.gui.sidebar.input;

import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;

public class SidebarNumberInput extends GuiSidebarElement {

    private final GetterSetter<Double> value;
    private final String text;
    private GuiNumberInput textField;

    public SidebarNumberInput(GetterSetter<Double> value, String text) {
        this.value = value;
        this.text = text;
    }

    @Override
    protected void init() {
        this.textField = new GuiNumberInput(
                0, 0, parent.elementWidth.get(), 20,
                this.value, this.text
        );
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.textField.setWidth(newWidth);
    }

    @Override
    public void drawComponent(double mouseX, double mouseY, float partialTicks) {
        textField.drawComponent(mouseX, mouseY, partialTicks);
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

    @Override public int getHeight() { return 20; }

    @Override public void updateScreen() {}
    @Override public void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int state) {}
}
