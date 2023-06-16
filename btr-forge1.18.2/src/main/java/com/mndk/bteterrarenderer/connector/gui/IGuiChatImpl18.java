package com.mndk.bteterrarenderer.connector.gui;

public class IGuiChatImpl18 implements IGuiChat {



    @Override
    public void init() {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String getInputFieldText() {
        return null;
    }

    @Override
    public boolean isInputFieldFocused() {
        return false;
    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public void setHeight(int height) {

    }

    @Override
    public void setInputFieldX(int x) {

    }

    @Override
    public void setInputFieldWidth(int width) {

    }

    @Override
    public void setInputFieldFocused(boolean focused) {

    }

    @Override
    public void setText(String newChatText, boolean shouldOverwrite) {

    }

    @Override
    public void drawInputFieldBox() {

    }

    @Override
    public void sendChatMessage(String s) {

    }

    @Override
    public void updateScreen() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void handleMouseInput() {

    }

    @Override
    public void handleMouseHover(double mouseX, double mouseY, float partialTicks) {

    }

    @Override
    public boolean handleMouseClick(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    public boolean inputFieldMouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }  // TODO finish this class

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
