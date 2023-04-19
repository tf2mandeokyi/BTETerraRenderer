package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.gui.IFontRenderer;
import lombok.Getter;
import lombok.Setter;

public class GuiCheckBoxImpl extends GuiButtonImpl {
    private static final int BOX_WIDTH = 11, BOX_HEIGHT = 11;

    @Getter @Setter
    private boolean checked;

    public GuiCheckBoxImpl(int x, int y, String text, boolean checked) {
        super(x, y, text);
        this.checked = checked;
        this.width = BOX_WIDTH + 2 + DependencyConnectorSupplier.INSTANCE.getMinecraftFontRenderer().getStringWidth(text);
        this.height = BOX_HEIGHT;
    }

    @Override
    public void drawButton(int mouseX, int mouseY, float partialTicks) {
        if(!this.visible) return;

        this.hovered = this.isMouseOnButton(mouseX, mouseY);
        GuiStaticConnector.INSTANCE.drawContinuousTexturedBox(
                BUTTON_TEXTURES,
                this.x, this.y, 0, 46,
                BOX_WIDTH, this.height, 200, 20,
                2, 3, 2, 2,
                this.zLevel
        );
        this.mouseDragged(mouseX, mouseY);

        int color = 0xE0E0E0;
        if(packedForegroundColor != 0)  color = packedForegroundColor;
        else if(!this.enabled)          color = 0xA0A0A0;

        IFontRenderer fontRenderer = DependencyConnectorSupplier.INSTANCE.getMinecraftFontRenderer();
        if (this.checked) {
            fontRenderer.drawCenteredStringWithShadow("x", this.x + BOX_WIDTH / 2f + 1, this.y + 1, 0xE0E0E0);
        }
        fontRenderer.drawStringWithShadow(text, this.x + BOX_WIDTH + 2, this.y + 2, color);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY)
    {
        if (this.enabled && this.visible && this.isMouseOnButton(mouseX, mouseY)) {
            this.checked = !this.checked;
            return true;
        }
        return false;
    }
}
