package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

public class DrawContextWrapperImpl extends DrawContextWrapper<DrawContext> {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = new Identifier("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED = new Identifier("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED = new Identifier("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX = new Identifier("widget/checkbox");
    private static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            new Identifier("widget/button"),
            new Identifier("widget/button_disabled"),
            new Identifier("widget/button_highlighted")
    );

    public DrawContextWrapperImpl(@Nonnull DrawContext delegate) {
        super(delegate);
    }

    public BufferBuilderWrapper<?> tessellatorBufferBuilder() {
        return new BufferBuilderWrapperImpl(Tessellator.getInstance().getBuffer());
    }

    public void translate(float x, float y, float z) {
        getThisWrapped().getMatrices().translate(x, y, z);
    }
    public void pushMatrix() {
        getThisWrapped().getMatrices().push();
    }
    public void popMatrix() {
        getThisWrapped().getMatrices().pop();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        Window window = MinecraftClient.getInstance().getWindow();
        if(window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = (float) window.getFramebufferWidth() / window.getScaledWidth();
        float scaleFactorY = (float) window.getFramebufferHeight() / window.getScaledHeight();

        Matrix4f matrix = getThisWrapped().getMatrices().peek().getPositionMatrix();
        Vector4f start = new Vector4f(relX, relY, 0, 1);
        Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
        start = matrix.transform(start);
        end = matrix.transform(end);

        int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
        int scissorY = (int) (window.getFramebufferHeight() - scaleFactorY * Math.max(start.y(), end.y()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.x() - end.x()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.y() - end.y()));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        boolean enabled = hoverState != AbstractWidgetCopy.HoverState.DISABLED;
        boolean focused = hoverState == AbstractWidgetCopy.HoverState.MOUSE_OVER;
        Identifier buttonTexture = BUTTON_TEXTURES.get(enabled, focused);
        getThisWrapped().drawGuiTexture(buttonTexture, x, y, width, height);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Identifier identifier = checked ?
                (focused ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED) :
                (focused ? CHECKBOX_HIGHLIGHTED : CHECKBOX);
        getThisWrapped().setShaderColor(1, 1, 1, 1);
        getThisWrapped().drawGuiTexture(identifier, x, y, width, height);
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
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Style style = styleWrapper.get();
        getThisWrapped().drawHoverEvent(textRenderer, style, x, y);
    }

    public int drawTextWithShadow(FontWrapper<?> fontWrapper, String string, float x, float y, int color) {
        TextRenderer textRenderer = fontWrapper.get();
        return getThisWrapped().drawTextWithShadow(textRenderer, string, (int) x, (int) y, color);
    }

    public int drawTextWithShadow(FontWrapper<?> fontWrapper, TextWrapper textWrapper, float x, float y, int color) {
        TextRenderer textRenderer = fontWrapper.get();
        Object textComponent = textWrapper.get();
        if(textComponent instanceof Text text) {
            return getThisWrapped().drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
        }
        else if(textComponent instanceof OrderedText text) {
            return getThisWrapped().drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
        }
        return 0;
    }
}
