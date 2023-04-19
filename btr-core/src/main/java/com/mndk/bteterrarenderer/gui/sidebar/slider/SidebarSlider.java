package com.mndk.bteterrarenderer.gui.sidebar.slider;

import com.mndk.bteterrarenderer.gui.components.GuiSliderImpl;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.util.function.Function;

public class SidebarSlider extends GuiSidebarElement {

    private GuiSliderImpl slider;

    private final GetterSetter<Double> doubleValue;
    private final GetterSetter<Integer> intValue;
    private final Function<Integer, Boolean> intAllowFunction;

    private final String prefix, suffix;
    private final double minValue, maxValue;
    private final boolean isDouble;

    public SidebarSlider(GetterSetter<Double> value,
                         String prefix, String suffix,
                         double minValue, double maxValue) {
        this.doubleValue = value;
        this.prefix = prefix; this.suffix = suffix;
        this.minValue = minValue; this.maxValue = maxValue;
        this.isDouble = true;

        this.intValue = null;
        this.intAllowFunction = null;
    }

    public SidebarSlider(GetterSetter<Integer> value,
                         String prefix, String suffix,
                         int minValue, int maxValue,
                         Function<Integer, Boolean> intAllowFunction) {
        this.intValue = value;
        this.intAllowFunction = intAllowFunction;
        this.prefix = prefix; this.suffix = suffix;
        this.minValue = minValue; this.maxValue = maxValue;
        this.isDouble = false;

        this.doubleValue = null;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    protected void init() {
        if(this.isDouble) {
            assert doubleValue != null;
            this.slider = new GuiSliderImpl(
                    0, 0,
                    parent.elementWidth.get(), 20,
                    prefix, suffix,
                    minValue, maxValue, doubleValue.get(),
                    true, true, false,
                    slider -> doubleValue.set(slider.getValue())
            );
        }
        else {
            assert intValue != null;
            this.slider = new GuiSliderImpl(
                    0, 0,
                    parent.elementWidth.get(), 20,
                    prefix, suffix,
                    (int) minValue, (int) maxValue, intValue.get(),
                    false, true, true,
                    slider -> intValue.set((int) slider.getValue())
            );
        }
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.slider.width = newWidth;
    }

    @Override
    public void updateScreen() {
        this.slider.updateSlider();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(this.slider.drawString && this.intAllowFunction != null) {
            this.slider.packedForegroundColor = intAllowFunction.apply(this.slider.getValueInt()) ? 0 : 0xFF0000;
        }
        this.slider.drawButton(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return this.slider.mousePressed(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.slider.mouseReleased(mouseX, mouseY);
    }

    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
}
