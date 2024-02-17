package com.mndk.bteterrarenderer.core.gui.mcfx.checkbox;

import com.mndk.bteterrarenderer.core.gui.mcfx.McFXElement;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.CheckBoxWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public class McFXCheckBox extends McFXElement {

    private CheckBoxWidgetCopy checkBox;
    private final String suffixText;
    private final PropertyAccessor<Boolean> propertyAccessor;

    public McFXCheckBox(PropertyAccessor<Boolean> propertyAccessor, String suffixText) {
        this.propertyAccessor = propertyAccessor;
        this.suffixText = suffixText;
    }

    @Override
    public int getPhysicalHeight() {
        return 20;
    }

    @Override
    protected void init() {
        this.checkBox = new CheckBoxWidgetCopy(0, 0, this.getWidth(), suffixText, propertyAccessor.get());
    }

    @Override
    public void onWidthChange() {
        this.checkBox.setWidth(this.getWidth());
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        return this.checkBox.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        this.checkBox.drawComponent(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.checkBox.mousePressed(mouseX, mouseY, mouseButton)) {
            propertyAccessor.set(this.checkBox.isChecked());
            return true;
        }
        return false;
    }
}
