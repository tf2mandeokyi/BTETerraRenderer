package com.mndk.bteterrarenderer.mcconnector.client.gui;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
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
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class GuiDrawContextWrapperImpl extends AbstractGuiDrawContextWrapper {

    private static final Identifier CHECKBOX = new Identifier("textures/gui/checkbox.png");

    @Nonnull public final MatrixStack delegate;

    public void translate(float x, float y, float z) {
        delegate.translate(x, y, z);
    }
    public void pushMatrix() {
        delegate.push();
    }
    public void popMatrix() {
        delegate.pop();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        if (window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = window.getScaleFactorX();
        float scaleFactorY = window.getScaleFactorY();

        Matrix4f matrix = delegate.peek().getPositionMatrix();
        Vector4f start = new Vector4f(relX, relY, 0, 1);
        Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
        start.transform(matrix);
        end.transform(matrix);

        int scissorX = (int) (scaleFactorX * Math.min(start.getX(), end.getX()));
        int scissorY = (int) (window.getPixelHeight() - scaleFactorY * Math.max(start.getY(), end.getY()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.getX() - end.getX()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.getY() - end.getY()));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }
    protected void glEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }
    protected void glDisableScissor() {
        RenderSystem.disableScissor();
    }

    public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
        Matrix4f matrix4f = delegate.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        quad.forEach(v -> bufferBuilder.vertex(matrix4f, v.x, v.y, z).color(color).next());
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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

        DrawableHelper.drawTexture(delegate, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        DrawableHelper.drawTexture(delegate, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        Matrix4f matrix4f = delegate.peek().getPositionMatrix();
        RenderSystem.setShaderTexture(0, CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        float textureSize = 20 / 64f;
        float u0 = focused ? textureSize : 0, v0 = checked ? textureSize : 0;
        float u1 = u0 + textureSize, v1 = v0 + textureSize;
        DrawableHelper.drawTexturedQuad(matrix4f, x, x+width, y, y+height, 0, u0, u1, v0, v1);
    }

    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(startX, endY, 0).next();
        bufferBuilder.vertex(endX, endY, 0).next();
        bufferBuilder.vertex(endX, startY, 0).next();
        bufferBuilder.vertex(startX, startY, 0).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float u2, float v1, float v2) {
        Matrix4f matrix4f = delegate.peek().getPositionMatrix();
        RenderSystem.setShaderTexture(0, ((ResourceLocationWrapperImpl) res).delegate());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexturedQuad(matrix4f, x, x+w, y, y+h, 0, u1, u2, v1, v2);
    }

    public void drawWholeNativeImage(@Nonnull NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        Matrix4f matrix4f = delegate.peek().getPositionMatrix();
        RenderSystem.setShaderTexture(0, ((NativeTextureWrapperImpl) allocatedTextureObject).delegate);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexturedQuad(matrix4f, x, x+w, y, y+h, 0, 0, 1, 0, 1);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

        Style style = ((StyleWrapperImpl) styleWrapper).delegate();
        guiScreen.renderTextHoverEffect(delegate, style, x, y);
    }

    public int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return textRenderer.drawWithShadow(delegate, string, x, y, color);
    }
}
