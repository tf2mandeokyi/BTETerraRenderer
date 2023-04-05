package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.gui.IGuiCheckBox;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class IGuiCheckBoxImpl extends GuiCheckBox implements IGuiCheckBox {
    public IGuiCheckBoxImpl(int id, int xPos, int yPos, String displayString, boolean isChecked) {
        super(id, xPos, yPos, displayString, isChecked);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDisplayString() { return displayString; }
    public boolean isEnabled() { return enabled; }
    public boolean isVisible() { return visible; }
    public boolean isHovered() { return hovered; }
    public int getPackedFGColour() { return packedFGColour; }
    public float getZLevel() { return zLevel; }

    public void setHeight(int height) { this.height = height; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDisplayString(String displayString) { this.displayString = displayString; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setHovered(boolean hovered) { this.hovered = hovered; }
    public void setPackedFGColour(int packedFGColour) { this.packedFGColour = packedFGColour; }
    public void setZLevel(float zLevel) { this.zLevel = zLevel; }

    public void drawButton(int mouseX, int mouseY, float partialTicks) {
        super.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
    }
    public void mouseDragged(int mouseX, int mouseY) {
        super.mouseDragged(Minecraft.getMinecraft(), mouseX, mouseY);
    }
    public boolean mousePressed(int mouseX, int mouseY) {
        return super.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
    }
}
