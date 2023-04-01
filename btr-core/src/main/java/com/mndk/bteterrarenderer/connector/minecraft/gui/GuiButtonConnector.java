package com.mndk.bteterrarenderer.connector.minecraft.gui;

public interface GuiButtonConnector extends GuiObjectConnector {
    int getWidth();
    int getHeight();
    int getX();
    int getY();
    int getDisplayString();
    boolean isEnabled();
    boolean isVisible();
    boolean isHovered();
    int getPackedFGColour();

    void setWidth(int width);
    void setHeight(int height);
    void setX(int x);
    void setY(int y);
    void setDisplayString(String displayString);
    void setEnabled(boolean enabled);
    void setVisible(boolean visible);
    void setHovered(boolean hovered);
    void setPackedFGColour(int packedFGColour);

    void drawButton(int mouseX, int mouseY, float partialTicks);
    void mouseDragged(int mouseX, int mouseY);
    void mouseReleased(int mouseX, int mouseY);
    boolean mousePressed(int mouseX, int mouseY);
    boolean isMouseOver();
}
