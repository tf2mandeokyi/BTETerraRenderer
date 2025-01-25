package com.mndk.bteterrarenderer.mcconnector.client.mcfx.input;

import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.GuiNumberInput;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;

public class McFXNumberInput extends McFXElement {

    private final PropertyAccessor<Double> value;
    private final String prefixText;
    private com.mndk.bteterrarenderer.mcconnector.client.gui.widget.GuiNumberInput textField;

    public McFXNumberInput(PropertyAccessor<Double> value, String prefixText) {
        this.value = value;
        this.prefixText = prefixText;
    }

    @Override
    protected void init() {
        this.textField = new GuiNumberInput(
                0, 0, this.getWidth(), 20,
                this.value, this.prefixText
        );
    }

    public void setPrefixColor(int prefixColor) {
        this.textField.setPrefixColor(prefixColor);
    }

    public void update() {
        this.textField.update();
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
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        return textField.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawElement(GuiDrawContextWrapper drawContextWrapper) {
        textField.drawComponent(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
       textField.mousePressed(mouseX, mouseY, mouseButton);
       return false;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        return textField.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        return textField.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean handleScreenEscape() {
        if (textField.isFocused()) {
            textField.setFocused(false);
            return false;
        }
        return true;
    }

    @Override public int getPhysicalHeight() { return 20; }
}
