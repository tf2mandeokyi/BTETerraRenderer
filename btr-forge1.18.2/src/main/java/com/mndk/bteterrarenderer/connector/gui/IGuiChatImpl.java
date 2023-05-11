package com.mndk.bteterrarenderer.connector.gui;

public class IGuiChatImpl /*implements IGuiChat*/ {  // TODO finish this class

//    private final OpenGuiChat delegate;
//
//    public IGuiChatImpl() {
//        this.delegate = new OpenGuiChat();
//    }
//
//    public void init() {
//        Minecraft mc = Minecraft.getMinecraft();
//        delegate.mc = mc;
//        delegate.setItemRender(mc.getRenderItem());
//        delegate.setFontRenderer(mc.fontRenderer);
//        delegate.initGui();
//    }
//
//    public int getWidth() { return delegate.width; }
//    public int getHeight() { return delegate.height; }
//    public String getInputFieldText() { return delegate.getInputField().getText(); }
//    public boolean isInputFieldFocused() { return delegate.getInputField().isFocused(); }
//
//    public void setWidth(int width) { delegate.width = width; }
//    public void setHeight(int height) { delegate.height = height; }
//    public void setInputFieldX(int x) { delegate.getInputField().x = x; }
//    public void setInputFieldWidth(int width) { delegate.getInputField().width = width; }
//    public void setInputFieldFocused(boolean focused) { delegate.getInputField().setFocused(focused); }
//    public void setText(String newChatText, boolean shouldOverwrite) { delegate.setText(newChatText, shouldOverwrite); }
//
//    public void drawInputFieldBox() { delegate.getInputField().drawTextBox(); }
//    public void sendChatMessage(String s) { delegate.sendChatMessage(s); }
//    public void updateScreen() { delegate.updateScreen(); }
//    public void keyTyped(char typedChar, int keyCode) throws IOException { delegate.keyTyped(typedChar, keyCode); }
//    public void handleMouseInput() throws IOException { delegate.handleMouseInput(); }
//
//    public void handleMouseHover(int mouseX, int mouseY, float partialTicks) {
//        ITextComponent itextcomponent = delegate.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
//        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
//            delegate.handleComponentHover(itextcomponent, mouseX, mouseY);
//        }
//    }
//    public boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
//        ITextComponent itextcomponent = delegate.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
//        if (itextcomponent != null) return delegate.handleComponentClick(itextcomponent);
//        else return false;
//    }
//    public boolean inputFieldMouseClicked(int mouseX, int mouseY, int mouseButton) {
//        return delegate.getInputField().mouseClicked(mouseX, mouseY, mouseButton);
//    }
//
//    private static class OpenGuiChat extends GuiChat {
//        public GuiTextField getInputField() { return inputField; }
//        public void setItemRender(RenderItem itemRender) { this.itemRender = itemRender; }
//        public void setFontRenderer(FontRenderer fontRenderer) { this.fontRenderer = fontRenderer; }
//        public void setText(@Nonnull String newChatText, boolean shouldOverwrite) { super.setText(newChatText, shouldOverwrite); }
//        public void keyTyped(char typedChar, int keyCode) throws IOException { super.keyTyped(typedChar, keyCode); }
//        public void handleComponentHover(@Nonnull ITextComponent component, int x, int y) { super.handleComponentHover(component, x, y); }
//    }
}
