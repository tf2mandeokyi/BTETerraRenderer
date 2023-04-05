package com.mndk.bteterrarenderer.connector.minecraft.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class IGuiChatImpl extends GuiChat implements IGuiChat {

    public void init() {
        this.mc = Minecraft.getMinecraft();
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        super.initGui();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getInputFieldText() { return inputField.getText(); }
    public boolean isInputFieldFocused() { return inputField.isFocused(); }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setInputFieldX(int x) { inputField.x = x; }
    public void setInputFieldWidth(int width) { inputField.width = width; }
    public void setInputFieldFocused(boolean focused) { inputField.setFocused(focused); }
    public void setText(String newChatText, boolean shouldOverwrite) { super.setText(newChatText, shouldOverwrite); }

    public void drawInputFieldBox() { inputField.drawTextBox(); }

    public void keyTyped(char typedChar, int keyCode) throws IOException { super.keyTyped(typedChar, keyCode); }
    public void handleMouseHover(int mouseX, int mouseY, float partialTicks) {
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }
    }
    public boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        if (itextcomponent != null) return this.handleComponentClick(itextcomponent);
        else return false;
    }
    public boolean inputFieldMouseClicked(int mouseX, int mouseY, int mouseButton) {
        return inputField.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
