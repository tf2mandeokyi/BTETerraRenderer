package com.mndk.bteterrarenderer.mcconnector.client.mcfx.slider;

import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.SliderWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

public class McFXSlider<T extends Number> extends McFXElement {

    private SliderWidgetCopy slider;

    private final PropertyAccessor.Ranged<T> value;

    private final String prefix, suffix;
    private final boolean isInteger;

    public McFXSlider(PropertyAccessor.Ranged<T> value,
                      String prefix, String suffix) {
        this.value = value;
        this.prefix = prefix; this.suffix = suffix;

        Class<?> valueClass = value.getPropertyClass();
        this.isInteger = valueClass != float.class && valueClass != Float.class &&
                valueClass != double.class && valueClass != Double.class;
    }

    @Override
    public int getPhysicalHeight() {
        return 20;
    }

    @Override
    protected void init() {
        assert value != null;
        this.slider = new SliderWidgetCopy(
                0, 0,
                this.getWidth(), 20,
                prefix, suffix,
                value.min().doubleValue(), value.max().doubleValue(), value.get().doubleValue(),
                !this.isInteger, true, this.isInteger,
                slider -> value.set(BTRUtil.doubleToNumber(value.getPropertyClass(), slider.getValue()))
        );
    }

    @Override
    public void onWidthChange() {
        this.slider.setWidth(this.getWidth());
    }

    @Override
    public void tick() {
        this.slider.tick();
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        return this.slider.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        if(this.slider.drawString) {
            boolean testResult;
            if(this.isInteger) {
                testResult = value.available(BTRUtil.doubleToNumber(value.getPropertyClass(), this.slider.getValue()));
            } else {
                testResult = this.value.available(BTRUtil.integerToNumber(value.getPropertyClass(), this.slider.getValueInt()));
            }
            this.slider.packedForegroundColor = testResult ? NULL_COLOR : ERROR_TEXT_COLOR;
        }
        this.slider.drawComponent(drawContextWrapper);
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
