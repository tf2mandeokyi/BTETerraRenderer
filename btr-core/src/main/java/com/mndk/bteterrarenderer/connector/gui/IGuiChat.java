package com.mndk.bteterrarenderer.connector.gui;

import java.io.IOException;

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
    void keyTyped(char typedChar, int keyCode) throws IOException;
    void handleMouseInput() throws IOException;
    void handleMouseHover(int mouseX, int mouseY, float partialTicks);
    boolean handleMouseClick(int mouseX, int mouseY, int mouseButton);
    boolean inputFieldMouseClicked(int mouseX, int mouseY, int mouseButton);
}
