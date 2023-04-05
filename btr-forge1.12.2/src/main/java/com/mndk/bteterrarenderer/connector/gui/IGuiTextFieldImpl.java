package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.gui.IGuiTextField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class IGuiTextFieldImpl extends GuiTextField implements IGuiTextField {

    public IGuiTextFieldImpl(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
        super(componentId, fontrendererObj, x, y, width, height);
    }

    public int getY() { return y; }
    public int getHeight() { return height; }
    public float getZLevel() { return zLevel; }

    public void setX(int x) { this.x = x; }
    public void setWidth(int width) { this.width = width; }
    public void setZLevel(float zLevel) { this.zLevel = zLevel; }
}
