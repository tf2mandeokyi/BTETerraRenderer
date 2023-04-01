package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.minecraft.ResourceLocationConnector;

public interface GuiSliderConnector extends GuiButtonConnector {
    boolean isDragging();
    boolean shouldDrawString();
    double getMinValue();
    double getMaxValue();
    double getValue();
    float getSliderValue();
    int getValueInt();
    ResourceLocationConnector getButtonTextures();
    String getPrefix();
    String getSuffix();
    SliderChangeHandler getSliderChangeHandler();

    void setDragging(boolean dragging);
    void setSliderValue(float sliderValue);

    void updateSlider();

    interface SliderChangeHandler {
        void handle(GuiSliderConnector slider);
    }
}
