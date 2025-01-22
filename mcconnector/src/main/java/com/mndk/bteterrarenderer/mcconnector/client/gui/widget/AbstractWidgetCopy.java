package com.mndk.bteterrarenderer.mcconnector.client.gui.widget;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.component.GuiComponentCopy;
import lombok.Getter;
import lombok.Setter;

/**
 * Copied from both 1.12.2's <code>net.minecraft.client.gui.GuiButton</code>
 * and 1.18.2's <code>net.minecraft.client.gui.components.AbstractWidget</code>
 */
@Getter @Setter
public abstract class AbstractWidgetCopy implements GuiComponentCopy {

    protected int x, y, width, height;
    protected String text;
    protected boolean hovered;
    public boolean enabled = true, visible = true;
    public int packedForegroundColor;
    private boolean focused;

    public AbstractWidgetCopy(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    protected HoverState getButtonHoverState(boolean mouseOver) {
        if (!this.enabled) return HoverState.DISABLED;
        else if (mouseOver) return HoverState.MOUSE_OVER;
        return HoverState.DEFAULT;
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        return this.hovered = !mouseHidden && this.isMouseOnWidget(mouseX, mouseY);
    }

    public void drawComponent(GuiDrawContextWrapper drawContextWrapper) {
        if (!this.visible) return;

        HoverState hoverState = this.getButtonHoverState(this.hovered);
        drawContextWrapper.drawButton(x, y, width, height, hoverState);
        this.drawBackground(drawContextWrapper);

        int color = NORMAL_TEXT_COLOR;
        if (packedForegroundColor != NULL_COLOR)  color = packedForegroundColor;
        else if (!this.enabled)           color = DISABLED_TEXT_COLOR;
        else if (this.hovered)            color = HOVERED_COLOR;

        String buttonText = this.text;
        int stringWidth = getDefaultFont().getWidth(buttonText);
        int ellipsisWidth = getDefaultFont().getWidth("...");

        if (stringWidth > width - 6 && stringWidth > ellipsisWidth) {
            buttonText = getDefaultFont().trimToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";
        }
        drawContextWrapper.drawCenteredTextWithShadow(getDefaultFont(), buttonText, this.x + this.width / 2f, this.y + (this.height - 8) / 2f, color);
    }

    public void drawBackground(GuiDrawContextWrapper drawContextWrapper) {}

    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        return this.enabled && this.visible && this.isMouseOnWidget(mouseX, mouseY);
    }

    public boolean isMouseOnWidget(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    public enum HoverState {
        DISABLED, DEFAULT, MOUSE_OVER
    }
}