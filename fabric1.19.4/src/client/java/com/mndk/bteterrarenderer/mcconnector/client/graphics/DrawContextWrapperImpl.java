package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

public class DrawContextWrapperImpl extends DrawContextWrapper<MatrixStack> {

    private static final Identifier CHECKBOX = new Identifier("textures/gui/checkbox.png");

    public DrawContextWrapperImpl(@Nonnull MatrixStack delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper<?> tessellatorBufferBuilder() {
        return new BufferBuilderWrapperImpl(Tessellator.getInstance().getBuffer());
    }

    public void translate(float x, float y, float z) {
        getThisWrapped().translate(x, y, z);
    }
    public void pushMatrix() {
        getThisWrapped().push();
    }
    public void popMatrix() {
        getThisWrapped().pop();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        if(window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = window.getScaleFactorX();
        float scaleFactorY = window.getScaleFactorY();

        Matrix4f matrix = getThisWrapped().peek().getPositionMatrix();
        Vector4f start = new Vector4f(relX, relY, 0, 1);
        Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
        start = matrix.transform(start);
        end = matrix.transform(end);

        int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
        int scissorY = (int) (window.getPixelHeight() - scaleFactorY * Math.max(start.y(), end.y()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.x() - end.x()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.y() - end.y()));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        DrawableHelper.drawTexture(getThisWrapped(), x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        DrawableHelper.drawTexture(getThisWrapped(), x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        float size = 20 / 64f;
        float u0 = focused ? size : 0, v0 = checked ? size : 0;
        float u1 = u0 + size, v1 = v0 + size;
        this.drawPosTexQuad(x, y, width, height, u0, v0, u1, v1);
    }

    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        this.drawPosQuad(startX, startY, endX - startX, endY - startY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
    }

    public void drawImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, res.get());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawPosTexQuad(x, y, w, h, u1, v1, u2, v2);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

        Style style = styleWrapper.get();
        guiScreen.renderTextHoverEffect(getThisWrapped(), style, x, y);
    }

    public int drawTextWithShadow(FontWrapper<?> fontWrapper, String string, float x, float y, int color) {
        TextRenderer textRenderer = fontWrapper.get();
        return textRenderer.drawWithShadow(getThisWrapped(), string, x, y, color);
    }

    public int drawTextWithShadow(FontWrapper<?> fontWrapper, TextWrapper textWrapper, float x, float y, int color) {
        TextRenderer textRenderer = fontWrapper.get();
        Object textComponent = textWrapper.get();
        if(textComponent instanceof Text text) {
            return textRenderer.drawWithShadow(getThisWrapped(), text, x, y, color);
        }
        else if(textComponent instanceof OrderedText text) {
            return textRenderer.drawWithShadow(getThisWrapped(), text, x, y, color);
        }
        return 0;
    }
}
