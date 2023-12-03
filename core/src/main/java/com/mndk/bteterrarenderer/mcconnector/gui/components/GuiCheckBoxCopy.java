package com.mndk.bteterrarenderer.mcconnector.gui.components;

import com.mndk.bteterrarenderer.mcconnector.gui.IFont;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GuiCheckBoxCopy extends GuiAbstractWidgetCopy {
    public static final int BOX_WIDTH = 11, BOX_HEIGHT = 11;
    private static final int BOX_MARGIN_RIGHT = 5;

    @Setter
    private boolean checked;

    public GuiCheckBoxCopy(int x, int y, String text, boolean checked) {
        super(x, y, BOX_WIDTH + BOX_MARGIN_RIGHT + IFont.DEFAULT.getStringWidth(text), BOX_HEIGHT, text);
        this.checked = checked;
    }

    @Override
    public void drawComponent(Object poseStack) {
        if(!this.visible) return;

        RawGuiManager.INSTANCE.drawCheckBox(poseStack, x, y, BOX_WIDTH, height, this.isFocused(), checked);

        int color = NORMAL_TEXT_COLOR;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = DISABLED_TEXT_COLOR;
        else if(this.hovered)            color = HOVERED_COLOR;

        IFont.DEFAULT.drawStringWithShadow(poseStack, text, this.x + BOX_WIDTH + BOX_MARGIN_RIGHT, this.y + 2, color);
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
