package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;

@Getter @Setter
public class IGuiSliderImpl extends GuiSlider implements IGuiSlider {

    private static final IResourceLocation buttonTexturesImpl = new IResourceLocationImpl(BUTTON_TEXTURES);
    private final SliderChangeHandler sliderChangeHandler;

    public IGuiSliderImpl(int componentId,
                          int x, int y, int width, int height,
                          String prefix, String suffix,
                          double minValue, double maxValue, double currentValue,
                          boolean showDecimal, boolean drawString,
                          IGuiSlider.SliderChangeHandler sliderChangeHandler) {
        super(componentId, x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, showDecimal, drawString);
        this.sliderChangeHandler = sliderChangeHandler;
        this.parent = slider -> sliderChangeHandler.handle(this);
    }

    public boolean isDragging() { return dragging; }
    public boolean shouldDrawString() { return drawString; }
    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    public double getSliderValue() { return sliderValue; }
    public IResourceLocation getButtonTextures() { return buttonTexturesImpl; }
    public String getPrefix() { return dispString; }
    public String getSuffix() { return suffix; }
    public SliderChangeHandler getSliderChangeHandler() { return sliderChangeHandler; }
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

    public void setDragging(boolean dragging) { this.dragging = dragging; }
    public void setSliderValue(double sliderValue) { this.sliderValue = sliderValue; }
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
