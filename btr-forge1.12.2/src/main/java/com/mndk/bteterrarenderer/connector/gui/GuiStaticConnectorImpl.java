package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.gui.components.GuiAbstractWidgetImpl;
import com.mndk.bteterrarenderer.gui.components.GuiButtonImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

@ConnectorImpl
@SuppressWarnings("unused")
public class GuiStaticConnectorImpl implements GuiStaticConnector {
    public void displayGuiScreen(AbstractGuiScreen gui) {
        Minecraft.getMinecraft().displayGuiScreen(new AbstractGuiScreenImpl(gui));
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, w, h, color);
    }

    @Override
    public void drawButton(int x, int y, int width, int height, GuiButtonImpl.HoverState hoverState) {
        int i = 0;
        switch(hoverState) {
            case DISABLED:          break;
            case DEFAULT:    i = 1; break;
            case MOUSE_OVER: i = 2; break;
        }
        GuiUtils.drawContinuousTexturedBox(
                OpenGuiButton.WIDGET_TEXTURES,
                x, y, 0, 46 + i * 20,
                width, height, 200, 20,
                2, 3, 2, 2,
                0
        );
    }

    @Override
    public void drawCheckBox(int x, int y, int width, int height, boolean checked) {
        this.drawButton(x, y, width, height, GuiAbstractWidgetImpl.HoverState.DISABLED);
        if (checked) {
            FontConnector.INSTANCE.drawCenteredStringWithShadow("x", x + width / 2f + 1, y + 1, 0xE0E0E0);
        }
    }

    @Override
    public void drawTextFieldHighlight(int startX, int startY, int endX, int endY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    private static class OpenGuiButton extends GuiButton {
        public static final ResourceLocation WIDGET_TEXTURES = BUTTON_TEXTURES;
        public OpenGuiButton(int buttonId, int x, int y, String buttonText) {
            super(0, 0, 0, "");
            throw new UnsupportedOperationException("Not allowed to use this class as a button!");
        }
    }
}
