package com.mndk.bteterrarenderer.connector.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class IGuiTextFieldImpl implements IGuiTextField {

    private final GuiTextField delegate;

    public IGuiTextFieldImpl(int componentId, int x, int y, int width, int height) {
        this.delegate = new GuiTextField(componentId, Minecraft.getMinecraft().fontRenderer, x, y, width, height);
    }

    public int getY() { return delegate.y; }
    public int getHeight() { return delegate.height; }
    public String getText() { return delegate.getText(); }

    public void setText(String s) { delegate.setText(s); }
    public void setTextColor(int color) { delegate.setTextColor(color); }
    public void setX(int x) { delegate.x = x; }
    public void setWidth(int width) { delegate.width = width; }
    public void setMaxStringLength(int length) { delegate.setMaxStringLength(length); }

    public void drawTextBox() { delegate.drawTextBox(); }
    public boolean textboxKeyTyped(char typedChar, int keyCode) { return delegate.textboxKeyTyped(typedChar, keyCode); }
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) { return delegate.mouseClicked(mouseX, mouseY, mouseButton); }
}
