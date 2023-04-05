package com.mndk.bteterrarenderer.connector.gui;

public interface IGuiTextField extends IGuiObject {
    
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

    boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
}
