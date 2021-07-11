package com.mndk.bteterrarenderer.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.function.Consumer;

public class GuiIntegerSlider extends GuiSlider {

    public GuiIntegerSlider(
            int id, int xPos, int yPos,
            int width, int height,
            String prefix, String suffix,
            int minVal, int maxVal, int currentVal,
            boolean drawStr, Consumer<Integer> par) {

        super(id, xPos, yPos, width, height, prefix, suffix, minVal, maxVal, currentVal, false, drawStr, slider -> par.accept(slider.getValueInt()));
    }



    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = this.getSliderValue(mouseX);
            updateSlider();
            this.dragging = true;
            return true;
        }
        else {
            return false;
        }
    }



    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = this.getSliderValue(mouseX);
                updateSlider();
            }
            this.drawSliderPositionBox();
        }
    }



    @Override
    public void updateSlider()
    {
        if (this.sliderValue < 0.0F) this.sliderValue = 0.0F;
        if (this.sliderValue > 1.0F) this.sliderValue = 1.0F;

        String val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));

        if(drawString) {
            displayString = dispString + val + suffix;
        }

        if (parent != null) {
            parent.onChangeSliderValue(this);
        }
    }



    private void drawSliderPositionBox() {
        GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 8, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
    }



    private float getSliderValue(int mouseX) {
        float oldValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
        int value = (int) Math.round((this.maxValue - this.minValue) * oldValue + this.minValue);
        return (float) (value - this.minValue) / (float) (this.maxValue - this.minValue);
    }
}
