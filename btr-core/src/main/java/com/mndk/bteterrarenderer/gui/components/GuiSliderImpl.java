package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;

public class GuiSliderImpl extends GuiButtonImpl {

    protected static final String NUMBER_FORMATTER_STRING = "%.4f";

    public String prefix, suffix;
    public boolean dragging, showDecimal, drawString, isIntegerSlider;
    public double sliderValue;
    public double minValue, maxValue;
    public SliderChangeHandler sliderChangeHandler;


    public GuiSliderImpl(int x, int y, int width, int height,
                         String prefix, String suffix,
                         double minValue, double maxValue, double currentValue,
                         boolean showDecimal, boolean drawString, boolean isIntegerSlider,
                         SliderChangeHandler sliderChangeHandler) {
        super(x, y, width, height, prefix);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sliderValue = (currentValue - minValue) / (maxValue - minValue);
        this.prefix = prefix;
        this.suffix = suffix;
        this.sliderChangeHandler = sliderChangeHandler;
        this.showDecimal = showDecimal;
        this.drawString = drawString;
        this.isIntegerSlider = isIntegerSlider;
        updateDisplayString();
    }


    @Override
    protected int getHoverState(boolean mouseOver) { return 0; }


    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        if(!this.visible) return;

        if(this.dragging) this.updateSliderValue(mouseX);
        GuiStaticConnector.INSTANCE.drawContinuousTexturedBox(
                BUTTON_TEXTURES, 
                this.x + (int) (this.sliderValue * (double)(this.width - 8)), this.y, 0, 66,
                8, this.height, 200, 20,
                2, 3, 2, 2,
                this.zLevel
        );
    }


    @Override
    public boolean mousePressed(int mouseX, int mouseY) {
        if(super.mousePressed(mouseX, mouseY)) {
            this.updateSliderValue(mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }


    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }


    public double getValue() {
        return sliderValue * (maxValue - minValue) + minValue;
    }
    public Integer getValueInt() {
        return (int) Math.round(this.getValue());
    }


    public void updateSliderValue(int mouseX) {
        this.sliderValue = (mouseX - (this.x - 4)) / (double) (this.width - 8);
        this.updateSlider();
    }


    public void updateSlider() {
        if(this.isIntegerSlider) {
            int intValue = (int) Math.round(this.getValue());
            this.sliderValue = (intValue - this.minValue) / (this.maxValue - this.minValue);
        }

        if (this.sliderValue < 0.0f) this.sliderValue = 0.0f;
        if (this.sliderValue > 1.0f) this.sliderValue = 1.0f;

        this.updateDisplayString();
    }


    private void updateDisplayString() {
        double value = this.getValue();
        if(!this.drawString) {
            this.text = "";
            return;
        }

        String valueString = showDecimal ?
                String.format(NUMBER_FORMATTER_STRING, value) :
                Integer.toString((int) Math.round(value));

        this.text = this.prefix + valueString + this.suffix;
        if(sliderChangeHandler != null) {
            sliderChangeHandler.handleSliderChange(this);
        }
    }

    public interface SliderChangeHandler {
        void handleSliderChange(GuiSliderImpl slider);
    }
}
