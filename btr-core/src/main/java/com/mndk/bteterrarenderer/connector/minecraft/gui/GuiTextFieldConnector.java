package com.mndk.bteterrarenderer.connector.minecraft.gui;

public interface GuiTextFieldConnector extends GuiObjectConnector {
    
    int getY();
    int getHeight();
    String getText();
    
    void setText(String s);
    void setTextColor(int color);
    void setX(int x);
    void setWidth(int width);
    void setMaxStringLength(int length);
    
    void drawTextBox();
    boolean textboxKeyTyped(char typedChar, int keyCode);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);
}
