package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckBoxWidgetCopy extends AbstractWidgetCopy {
    public static final int BOX_WIDTH = 11, BOX_HEIGHT = 11;
    private static final int BOX_MARGIN_RIGHT = 5;

    private boolean checked;

    public CheckBoxWidgetCopy(int x, int y, String text, boolean checked) {
        super(x, y, BOX_WIDTH + BOX_MARGIN_RIGHT + FontRenderer.DEFAULT.getStringWidth(text), BOX_HEIGHT, text);
        this.checked = checked;
    }

    @Override
    public void drawComponent(DrawContextWrapper drawContextWrapper) {
        if(!this.visible) return;

        RawGuiManager.INSTANCE.drawCheckBox(drawContextWrapper, x, y, BOX_WIDTH, height, this.isFocused(), checked);

        int color = NORMAL_TEXT_COLOR;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = DISABLED_TEXT_COLOR;
        else if(this.hovered)            color = HOVERED_COLOR;

        FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper, text, this.x + BOX_WIDTH + BOX_MARGIN_RIGHT, this.y + 2, color);
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
