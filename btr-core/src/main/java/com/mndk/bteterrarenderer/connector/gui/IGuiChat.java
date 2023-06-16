package com.mndk.bteterrarenderer.connector.gui;

public interface IGuiChat {

    void init();

    int getWidth();
    int getHeight();
    String getInputFieldText();
    boolean isInputFieldFocused();

    void setWidth(int width);
    void setHeight(int height);
    void setInputFieldX(int x);
    void setInputFieldWidth(int width);
    void setInputFieldFocused(boolean focused);
    void setText(String newChatText, boolean shouldOverwrite);

    void drawInputFieldBox();
    void sendChatMessage(String s);

    void updateScreen();
    void keyTyped(char typedChar, int keyCode);
    void handleMouseInput();
    void handleMouseHover(double mouseX, double mouseY, float partialTicks);
    boolean handleMouseClick(double mouseX, double mouseY, int mouseButton);
    boolean inputFieldMouseClicked(double mouseX, double mouseY, int mouseButton);
}
