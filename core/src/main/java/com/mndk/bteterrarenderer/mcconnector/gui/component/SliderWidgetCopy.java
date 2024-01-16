package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

/**
 * Copied from 1.18.2's net.minecraft.client.gui.components.AbstractSliderButton
 */
public class SliderWidgetCopy extends AbstractWidgetCopy {

    protected static final String NUMBER_FORMATTER_STRING = "%.4f";

    public String prefix, suffix;
    public boolean dragging, showDecimal, drawString, isIntegerSlider;
    public double sliderValue;
    public double minValue, maxValue;
    public SliderChangeHandler sliderChangeHandler;


    public SliderWidgetCopy(int x, int y, int width, int height,
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
    protected HoverState getButtonHoverState(boolean mouseOver) { return HoverState.DISABLED; }


    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        if(this.dragging) this.updateSliderValue(mouseX);
        return super.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawBackground(DrawContextWrapper drawContextWrapper) {
        if(!this.visible) return;

        RawGuiManager.INSTANCE.drawButton(drawContextWrapper,
                this.x + (int) (this.sliderValue * (double)(this.width - 8)), this.y,
                8, this.height, this.hovered ? HoverState.MOUSE_OVER : HoverState.DEFAULT
        );
    }


    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(super.mousePressed(mouseX, mouseY, mouseButton)) {
            this.updateSliderValue(mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.dragging = false;
        return true;
    }


    public double getValue() {
        return sliderValue * (maxValue - minValue) + minValue;
    }
    public Integer getValueInt() {
        return (int) Math.round(this.getValue());
    }


    public void updateSliderValue(double mouseX) {
        this.sliderValue = (mouseX - (this.x + 4)) / (double) (this.width - 8);
        this.tick();
    }


    public void tick() {
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
        void handleSliderChange(SliderWidgetCopy slider);
    }
}
