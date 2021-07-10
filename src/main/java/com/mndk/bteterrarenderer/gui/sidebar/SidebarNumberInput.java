package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.util.GetterSetter;
import com.mndk.bteterrarenderer.util.StringToNumber;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class SidebarNumberInput extends GuiSidebarElement {



    private final GetterSetter<Double> value;
    private final String text;
    private int textWidth;

    private GuiTextField textField;
    private boolean numberValidated;



    public SidebarNumberInput(GetterSetter<Double> value, String text) {
        this.value = value;
        this.text = text;
    }



    @Override
    protected void init() {
        this.textWidth = this.fontRenderer.getStringWidth(text) + 5;
        this.textField = new GuiTextField(
                -1, this.fontRenderer,
                textWidth, 0,
                parent.elementWidth.get() - textWidth, 20
        );
        this.textField.setText(StringToNumber.formatNicely(value.get()));
        this.textField.setMaxStringLength(50);
        this.numberValidated = true;
    }



    @Override
    public void onWidthChange(int newWidth) {
        this.textField.width = newWidth - this.textWidth;
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawString(
                this.fontRenderer, this.text,
                0, (20 - this.fontRenderer.FONT_HEIGHT) / 2,
                numberValidated ? 0xFFFFFF : 0xFF0000
        );
        textField.drawTextBox();
    }



    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        textField.mouseClicked(mouseX, mouseY, mouseButton);
    }



    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        if(textField.textboxKeyTyped(key, keyCode)) {
            String currentStr = textField.getText();
            textField.setTextColor((numberValidated = StringToNumber.validate(currentStr)) ? 0xFFFFFF : 0xFF0000);
            if(numberValidated) {
                value.set(Double.parseDouble(currentStr));
            }
        }
    }



    @Override public int getHeight() { return 20; }



    @Override public void updateScreen() {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
