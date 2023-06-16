package com.mndk.bteterrarenderer.connector.gui;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class IGuiChatImpl12 implements IGuiChat {

    private final OpenGuiChat delegate;

    public IGuiChatImpl12() {
        this.delegate = new OpenGuiChat();
    }

    public void init() {
        Minecraft mc = Minecraft.getMinecraft();
        delegate.mc = mc;
        delegate.setItemRender(mc.getRenderItem());
        delegate.setFontRenderer(mc.fontRenderer);
        delegate.initGui();
    }

    public int getWidth() { return delegate.width; }
    public int getHeight() { return delegate.height; }
    public String getInputFieldText() { return delegate.getInputField().getText(); }
    public boolean isInputFieldFocused() { return delegate.getInputField().isFocused(); }

    public void setWidth(int width) { delegate.width = width; }
    public void setHeight(int height) { delegate.height = height; }
    public void setInputFieldX(int x) { delegate.getInputField().x = x; }
    public void setInputFieldWidth(int width) { delegate.getInputField().width = width; }
    public void setInputFieldFocused(boolean focused) { delegate.getInputField().setFocused(focused); }
    public void setText(String newChatText, boolean shouldOverwrite) { delegate.setText(newChatText, shouldOverwrite); }

    public void drawInputFieldBox() { delegate.getInputField().drawTextBox(); }
    public void sendChatMessage(String s) { delegate.sendChatMessage(s); }
    public void updateScreen() { delegate.updateScreen(); }
    public void keyTyped(char typedChar, int keyCode) { delegate.keyTyped(typedChar, keyCode); }
    @SneakyThrows
    public void handleMouseInput() { delegate.handleMouseInput(); }

    public void handleMouseHover(double mouseX, double mouseY, float partialTicks) {
        ITextComponent itextcomponent = delegate.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            delegate.handleComponentHover(itextcomponent, (int) mouseX, (int) mouseY);
        }
    }
    public boolean handleMouseClick(double mouseX, double mouseY, int mouseButton) {
        ITextComponent itextcomponent = delegate.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        if (itextcomponent != null) return delegate.handleComponentClick(itextcomponent);
        else return false;
    }
    public boolean inputFieldMouseClicked(double mouseX, double mouseY, int mouseButton) {
        return delegate.getInputField().mouseClicked((int) mouseX, (int) mouseY, mouseButton);
    }

    private static class OpenGuiChat extends GuiChat {
        public GuiTextField getInputField() { return inputField; }
        public void setItemRender(RenderItem itemRender) { this.itemRender = itemRender; }
        public void setFontRenderer(FontRenderer fontRenderer) { this.fontRenderer = fontRenderer; }
        public void setText(@Nonnull String newChatText, boolean shouldOverwrite) { super.setText(newChatText, shouldOverwrite); }
        @SneakyThrows
        public void keyTyped(char typedChar, int keyCode) { super.keyTyped(typedChar, keyCode); }
        public void handleComponentHover(@Nonnull ITextComponent component, int x, int y) { super.handleComponentHover(component, x, y); }
    }
}
