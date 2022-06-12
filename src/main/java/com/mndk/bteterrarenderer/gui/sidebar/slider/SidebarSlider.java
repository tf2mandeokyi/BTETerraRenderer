package com.mndk.bteterrarenderer.gui.sidebar.slider;

import com.mndk.bteterrarenderer.gui.components.GuiIntegerSlider;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraftforge.fml.client.config.GuiSlider;

import javax.annotation.Nonnull;

public class SidebarSlider extends GuiSidebarElement {

    private GuiSlider slider;

    private final GetterSetter<Double> doubleValue;
    private final GetterSetter<Integer> intValue;

    private final String prefix, suffix;
    private final double minValue, maxValue;
    private final boolean isDouble;

    public SidebarSlider(@Nonnull GetterSetter<Double> value, String prefix, String suffix, double minValue, double maxValue) {
        this.doubleValue = value;
        this.prefix = prefix; this.suffix = suffix;
        this.minValue = minValue; this.maxValue = maxValue;
        this.isDouble = true;

        this.intValue = null;
    }

    public SidebarSlider(@Nonnull GetterSetter<Integer> value, String prefix, String suffix, int minValue, int maxValue) {
        this.intValue = value;
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
            this.slider = new GuiSlider(
                    -1,
                    0, 0,
                    parent.elementWidth.get(), 20,
                    prefix, suffix,
                    minValue, maxValue, doubleValue.get(),
                    true, true,
                    slider -> doubleValue.set(slider.getValue())
            );
        }
        else {
            assert intValue != null;
            this.slider = new GuiIntegerSlider(
                    -1,
                    0, 0,
                    parent.elementWidth.get(), 20,
                    prefix, suffix,
                    (int) minValue, (int) maxValue, intValue.get(),
                    true, intValue::set
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
        this.slider.drawButton(parent.mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return this.slider.mousePressed(parent.mc, mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.slider.mouseReleased(mouseX, mouseY);
    }

    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
}
