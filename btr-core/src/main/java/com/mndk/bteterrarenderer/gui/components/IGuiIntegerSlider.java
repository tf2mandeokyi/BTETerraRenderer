package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.gui.IGuiSlider;

import java.util.function.Consumer;
import java.util.function.Function;

public class IGuiIntegerSlider implements IGuiSlider {


    private final IGuiSlider parent;
    private final Function<Integer, Boolean> allowFunction;


    public IGuiIntegerSlider(
            int id, int xPos, int yPos,
            int width, int height,
            String prefix, String suffix,
            int minVal, int maxVal, int currentVal, boolean drawStr,
            Consumer<Integer> par, Function<Integer, Boolean> allowFunction
    ) {
        this.parent = DependencyConnectorSupplier.INSTANCE.newGuiSlider(
                id, xPos, yPos, width, height,
                prefix, suffix,
                minVal, maxVal, currentVal, false, drawStr,
                slider -> {
                    int sliderValue = slider.getValueInt();
                    if(allowFunction.apply(sliderValue)) par.accept(sliderValue);
                }
        );
        this.allowFunction = allowFunction;
    }


    public boolean mousePressed(int mouseX, int mouseY) {
        if (parent.mousePressed(mouseX, mouseY)) {
            parent.setSliderValue(this.getSliderValue(mouseX));
            updateSlider();
            parent.setDragging(true);
            return true;
        }
        else return false;
    }


    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        if (parent.isVisible()) {
            if (parent.isDragging()) {
                parent.setSliderValue(this.getSliderValue(mouseX));
                updateSlider();
            }
            this.drawSliderPositionBox();
        }
    }


    public void updateSlider() {
        if (parent.getSliderValue() < 0) parent.setSliderValue(0);
        if (parent.getSliderValue() > 1) parent.setSliderValue(1);

        int val = parent.getValueInt();
        String valString = Integer.toString(val);
        if(parent.shouldDrawString()) {
            parent.setDisplayString(parent.getPrefix() + valString + parent.getSuffix());
            parent.setPackedFGColour(allowFunction.apply(val) ? 0 : 0xFF0000);
        }
        if (parent.getSliderChangeHandler() != null) parent.getSliderChangeHandler().handle(parent);
    }


    private void drawSliderPositionBox() {
        GuiStaticConnector.INSTANCE.drawContinuousTexturedBox(
                parent.getButtonTextures(),
                parent.getX() + (int)(parent.getSliderValue() * (float)(parent.getWidth() - 8)), parent.getY(),
                0, 66,
                8, parent.getHeight(),
                200, 20,
                2, 3, 2, 2,
                parent.getZLevel());
    }


    private float getSliderValue(int mouseX) {
        float oldValue = (float)(mouseX - (parent.getX() + 4)) / (float)(parent.getWidth() - 8);
        int value = (int) Math.round((parent.getMaxValue() - parent.getMinValue()) * oldValue + parent.getMinValue());
        return (float) ((value - parent.getMinValue()) / (parent.getMaxValue() - parent.getMinValue()));
    }


    // I hate writing these ;-;
    @Override public boolean isMouseOver() { return parent.isMouseOver(); }
    @Override public int getWidth() { return parent.getWidth(); }
    @Override public int getHeight() { return parent.getHeight(); }
    @Override public int getX() { return parent.getX(); }
    @Override public int getY() { return parent.getY(); }
    @Override public String getDisplayString() { return parent.getDisplayString(); }
    @Override public boolean isEnabled() { return parent.isEnabled(); }
    @Override public boolean isVisible() { return parent.isVisible(); }
    @Override public boolean isHovered() { return parent.isHovered(); }
    @Override public int getPackedFGColour() { return parent.getPackedFGColour(); }
    @Override public void setWidth(int width) { parent.setWidth(width); }
    @Override public void setHeight(int height) { parent.setHeight(height); }
    @Override public void setX(int x) { parent.setX(x); }
    @Override public void setY(int y) { parent.setY(y); }
    @Override public void setDisplayString(String displayString) { parent.setDisplayString(displayString); }
    @Override public void setEnabled(boolean enabled) { parent.setEnabled(enabled); }
    @Override public void setVisible(boolean visible) { parent.setVisible(visible); }
    @Override public void setHovered(boolean hovered) { parent.setHovered(hovered); }
    @Override public void setPackedFGColour(int packedFGColour) { parent.setPackedFGColour(packedFGColour); }
    @Override public void drawButton(int mouseX, int mouseY, float partialTicks) { parent.drawButton(mouseX, mouseY, partialTicks); }
    @Override public void mouseReleased(int mouseX, int mouseY) { parent.mouseReleased(mouseX, mouseY); }
    @Override public boolean isDragging() { return parent.isDragging(); }
    @Override public boolean shouldDrawString() { return parent.shouldDrawString(); }
    @Override public double getMinValue() { return parent.getMinValue(); }
    @Override public double getMaxValue() { return parent.getMaxValue();}
    @Override public double getValue() { return parent.getValue(); }
    @Override public double getSliderValue() { return parent.getSliderValue(); }
    @Override public int getValueInt() { return parent.getValueInt(); }
    @Override public IResourceLocation getButtonTextures() { return parent.getButtonTextures(); }
    @Override public String getPrefix() { return parent.getPrefix(); }
    @Override public String getSuffix() { return parent.getSuffix(); }
    @Override public SliderChangeHandler getSliderChangeHandler() { return parent.getSliderChangeHandler(); }
    @Override public void setDragging(boolean dragging) { parent.setDragging(dragging); }
    @Override public void setSliderValue(double sliderValue) { parent.setSliderValue(sliderValue); }
    @Override public float getZLevel() { return parent.getZLevel(); }
    @Override public void setZLevel(float zLevel) { parent.setZLevel(zLevel); }
}
