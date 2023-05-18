package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import lombok.Getter;
import lombok.Setter;

public class GuiCheckBoxImpl extends GuiAbstractWidgetImpl {
    public static final int BOX_WIDTH = 11, BOX_HEIGHT = 11;
    private static final int BOX_MARGIN_RIGHT = 5;
    private static final int TEXT_COLOR = 0xA0A0A0;

    @Getter @Setter
    private boolean checked;

    public GuiCheckBoxImpl(int x, int y, String text, boolean checked) {
        super(x, y, BOX_WIDTH + 2 + FontConnector.INSTANCE.getStringWidth(text), BOX_HEIGHT, text);
        this.checked = checked;
    }

    @Override
    public void drawComponent(double mouseX, double mouseY, float partialTicks) {
        if(!this.visible) return;

        this.hovered = this.isMouseOnWidget(mouseX, mouseY);
        GuiStaticConnector.INSTANCE.drawCheckBox(x, y, BOX_WIDTH, height, checked);

        int color = 0xE0E0E0;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = TEXT_COLOR;

        FontConnector.INSTANCE.drawStringWithShadow(text, this.x + BOX_WIDTH + BOX_MARGIN_RIGHT, this.y + 2, color);
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
