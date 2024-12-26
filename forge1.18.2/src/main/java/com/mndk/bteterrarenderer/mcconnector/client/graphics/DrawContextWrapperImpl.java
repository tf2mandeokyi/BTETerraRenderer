package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawModeEnum;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.VertexFormatEnum;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class DrawContextWrapperImpl extends AbstractDrawContextWrapper<PoseStack> {

    private static final ResourceLocation CHECKBOX = new ResourceLocation("textures/gui/checkbox.png");

    public DrawContextWrapperImpl(@Nonnull PoseStack delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper begin(DrawModeEnum glMode, VertexFormatEnum vertexFormat) {
        VertexFormat.Mode drawMode = switch (glMode) {
            case TRIANGLES -> VertexFormat.Mode.TRIANGLES;
            case QUADS -> VertexFormat.Mode.QUADS;
        };
        VertexFormat format = switch (vertexFormat) {
            case POSITION_COLOR_TEXTURE_LIGHT_NORMAL -> DefaultVertexFormat.BLOCK;
            case POSITION_TEXTURE_COLOR_NORMAL -> DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;
            case POSITION_TEXTURE_COLOR -> DefaultVertexFormat.POSITION_TEX_COLOR;
            case POSITION_COLOR -> DefaultVertexFormat.POSITION_COLOR;
            case POSITION_TEXTURE -> DefaultVertexFormat.POSITION_TEX;
            case POSITION -> DefaultVertexFormat.POSITION;
        };
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(drawMode, format);
        return new BufferBuilderWrapperImpl(builder);
    }

    public void translate(float x, float y, float z) {
        getWrapped().translate(x, y, z);
    }
    public void pushMatrix() {
        getWrapped().pushPose();
    }
    public void popMatrix() {
        getWrapped().popPose();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        if (window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = window.getScaleFactorX();
        float scaleFactorY = window.getScaleFactorY();

        Matrix4f matrix = getWrapped().last().pose();
        Vector4f start = new Vector4f(relX, relY, 0, 1);
        Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
        start.transform(matrix);
        end.transform(matrix);

        int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
        int scissorY = (int) (window.getPixelHeight() - scaleFactorY * Math.max(start.y(), end.y()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.x() - end.x()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.y() - end.y()));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        PoseStack poseStack = getWrapped();
        GuiComponent.blit(poseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        GuiComponent.blit(poseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        float size = 20 / 64f;
        float u1 = focused ? size : 0, v1 = checked ? size : 0;
        float u2 = u1 + size, v2 = v1 + size;
        this.drawPosTexQuad(x, y, width, height, u1, v1, u2, v2);
    }

    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        this.drawPosQuad(startX, startY, endX - startX, endY - startY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ((ResourceLocationWrapperImpl) res).getWrapped());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawPosTexQuad(x, y, w, h, u1, v1, u2, v2);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

        Style style = ((StyleWrapperImpl) styleWrapper).getWrapped();
        guiScreen.renderComponentHoverEffect(getWrapped(), style, x, y);
    }

    public int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color) {
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        return font.drawShadow(getWrapped(), string, x, y, color);
    }
}
