package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.gui.IFontRenderer;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public class GuiButtonImpl extends GuiObjectImpl {

    protected static final IResourceLocation BUTTON_TEXTURES = DependencyConnectorSupplier.INSTANCE.getWidgetTextures();
    public int x, y, width, height;
    public String text;
    public boolean enabled = true, visible = true;
    protected boolean hovered;
    public int packedForegroundColor;

    public GuiButtonImpl(int x, int y, String buttonText)
    {
        this(x, y, 200, 20, buttonText);
    }

    public GuiButtonImpl(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    protected int getHoverState(boolean mouseOver) {
        if(!this.enabled) return 0;
        else if(mouseOver) return 2;
        return 1;
    }

    public void drawButton(int mouseX, int mouseY, float partialTicks) {
        if(!this.visible) return;

        this.hovered = this.isMouseOnButton(mouseX, mouseY);
        int k = this.getHoverState(this.hovered);
        GuiStaticConnector.INSTANCE.drawContinuousTexturedBox(
                BUTTON_TEXTURES,
                this.x, this.y, 0, 46 + k * 20,
                this.width, this.height, 200, 20,
                2, 3, 2, 2,
                this.zLevel
        );
        this.mouseDragged(mouseX, mouseY);

        int color = 0xE0E0E0;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = 0xA0A0A0;
        else if(this.hovered)           color = 0xFFFFA0;

        IFontRenderer fontRenderer = DependencyConnectorSupplier.INSTANCE.getMinecraftFontRenderer();
        String buttonText = this.text;
        int stringWidth = fontRenderer.getStringWidth(buttonText);
        int ellipsisWidth = fontRenderer.getStringWidth("...");

        if (stringWidth > width - 6 && stringWidth > ellipsisWidth) {
            buttonText = fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";
        }
        fontRenderer.drawCenteredStringWithShadow(buttonText, this.x + this.width / 2f, this.y + (this.height - 8) / 2f, color);
    }

    public void mouseDragged(int mouseX, int mouseY) {}
    public void mouseReleased(int mouseX, int mouseY) {}
    public boolean mousePressed(int mouseX, int mouseY) {
        return this.enabled && this.visible && this.isMouseOnButton(mouseX, mouseY);
    }

    public boolean isMouseOnButton(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

}
