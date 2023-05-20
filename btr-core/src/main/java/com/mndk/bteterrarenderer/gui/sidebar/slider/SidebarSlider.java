package com.mndk.bteterrarenderer.gui.sidebar.slider;

import com.mndk.bteterrarenderer.gui.components.GuiSliderImpl;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.util.function.Predicate;

public class SidebarSlider extends GuiSidebarElement {

    private GuiSliderImpl slider;

    private final GetterSetter<Double> doubleValue;
    private final GetterSetter<Integer> intValue;
    private final Predicate<Integer> intValidator;

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
        this.intValidator = null;
    }

    public SidebarSlider(GetterSetter<Integer> value,
                         String prefix, String suffix,
                         int minValue, int maxValue,
                         Predicate<Integer> intValidator) {
        this.intValue = value;
        this.intValidator = intValidator;
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
                    parent.elementWidth.get().intValue(), 20,
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
                    parent.elementWidth.get().intValue(), 20,
                    prefix, suffix,
                    (int) minValue, (int) maxValue, intValue.get(),
                    false, true, true,
                    slider -> intValue.set((int) slider.getValue())
            );
        }
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.slider.setWidth((int) newWidth);
    }

    @Override
    public void updateScreen() {
        this.slider.updateSlider();
    }

    @Override
    public void drawComponent(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        if(this.slider.drawString && this.intValidator != null) {
            this.slider.packedForegroundColor = intValidator.test(this.slider.getValueInt()) ? 0 : 0xFF0000;
        }
        this.slider.drawComponent(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return this.slider.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return this.slider.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
