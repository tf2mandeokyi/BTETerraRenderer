package com.mndk.bteterrarenderer.mcconnector.gui.widget;

import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckBoxWidgetCopy extends AbstractWidgetCopy {
    public static final int BOX_WIDTH = 11, BOX_HEIGHT = 11;
    private static final int BOX_MARGIN_RIGHT = 5;

    private boolean checked;

    public CheckBoxWidgetCopy(int x, int y, String text, boolean checked) {
        super(x, y, BOX_WIDTH + BOX_MARGIN_RIGHT + FontWrapper.DEFAULT.getWidth(text), BOX_HEIGHT, text);
        this.checked = checked;
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        if(!this.visible) return;

        drawContextWrapper.drawCheckBox(x, y, BOX_WIDTH, height, this.isFocused(), checked);

        int color = NORMAL_TEXT_COLOR;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = DISABLED_TEXT_COLOR;
        else if(this.hovered)            color = HOVERED_COLOR;

        drawContextWrapper.drawTextWithShadow(FontWrapper.DEFAULT, text, this.x + BOX_WIDTH + BOX_MARGIN_RIGHT, this.y + 2, color);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (super.mousePressed(mouseX, mouseY, mouseButton)) {
            this.checked = !this.checked;
            return true;
        }
        return false;
    }
}
