package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl12;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.gui.components.GuiAbstractWidgetImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

@ConnectorImpl
@SuppressWarnings("unused")
public class GuiStaticConnectorImpl12 implements GuiStaticConnector {
    public void displayGuiScreen(AbstractGuiScreen gui) {
        Minecraft.getMinecraft().displayGuiScreen(new AbstractGuiScreenImpl12(gui));
    }

    @Override
    public void fillQuad(Object poseStack, GraphicsQuad<GraphicsQuad.Pos> quad, int color) {
        GraphicsQuad.Pos v0 = quad.get(0), v1 = quad.get(1), v2 = quad.get(2), v3 = quad.get(3);

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);

        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(v0.x, v0.y, v0.z).endVertex();
        bufferBuilder.pos(v1.x, v1.y, v1.z).endVertex();
        bufferBuilder.pos(v2.x, v2.y, v2.z).endVertex();
        bufferBuilder.pos(v3.x, v3.y, v3.z).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetImpl.HoverState hoverState) {
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
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        this.drawButton(poseStack, x, y, width, height, GuiAbstractWidgetImpl.HoverState.DISABLED);
        if (checked) {
            FontConnector.INSTANCE.drawCenteredStringWithShadow(poseStack, "x", x + width / 2f + 1, y + 1, 0xE0E0E0);
        }
    }

    @Override
    public void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY) {
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

    @Override
    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        ResourceLocation resourceLocation = ((IResourceLocationImpl12) res).getDelegate();
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y+h, 0).tex(u1, v2).endVertex();
        bufferbuilder.pos(x+w, y+h, 0).tex(u2, v2).endVertex();
        bufferbuilder.pos(x+w, y, 0).tex(u2, v1).endVertex();
        bufferbuilder.pos(x, y, 0).tex(u1, v1).endVertex();
        tessellator.draw();
    }

    private static class OpenGuiButton extends GuiButton {
        public static final ResourceLocation WIDGET_TEXTURES = BUTTON_TEXTURES;
        public OpenGuiButton(int buttonId, int x, int y, String buttonText) {
            super(0, 0, 0, "");
            throw new UnsupportedOperationException("Not allowed to use this class as a button!");
        }
    }
}
