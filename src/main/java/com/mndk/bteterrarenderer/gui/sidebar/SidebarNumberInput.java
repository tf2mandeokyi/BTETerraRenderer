package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.io.IOException;

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
                -1, this.fontRenderer,
                0, 0, parent.elementWidth.get(), 20,
                this.value, this.text
        );
    }



    @Override
    public void onWidthChange(int newWidth) {
        this.textField.setWidth(newWidth);
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        textField.drawTextBox();
    }



    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
       textField.mouseClicked(mouseX, mouseY, mouseButton);
       return false;
    }



    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        textField.textboxKeyTyped(key, keyCode);
    }



    @Override public int getHeight() { return 20; }



    @Override public void updateScreen() {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
