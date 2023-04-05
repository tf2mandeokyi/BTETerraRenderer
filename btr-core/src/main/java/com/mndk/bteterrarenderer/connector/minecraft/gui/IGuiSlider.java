package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public interface IGuiSlider extends IGuiButton {
    boolean isDragging();
    boolean shouldDrawString();
    double getMinValue();
    double getMaxValue();
    double getValue();
    double getSliderValue();
    int getValueInt();
    IResourceLocation getButtonTextures();
    String getPrefix();
    String getSuffix();
    SliderChangeHandler getSliderChangeHandler();

    void setDragging(boolean dragging);
    void setSliderValue(double sliderValue);

    void updateSlider();

    interface SliderChangeHandler {
        void handle(IGuiSlider slider);
    }
}
