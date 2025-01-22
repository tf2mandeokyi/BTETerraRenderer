package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class GuiDrawContextWrapperImpl extends AbstractGuiDrawContextWrapper {

    private static final Identifier CHECKBOX_TEXTURE = new Identifier("textures/gui/checkbox.png");

    @Nonnull public final DrawContext delegate;

    public void translate(float x, float y, float z) {
        delegate.getMatrices().translate(x, y, z);
    }
    public void pushMatrix() {
        delegate.getMatrices().push();
    }
    public void popMatrix() {
        delegate.getMatrices().pop();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        if (window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = window.getScaleFactorX();
        float scaleFactorY = window.getScaleFactorY();

        Matrix4f matrix = delegate.getMatrices().peek().getPositionMatrix();
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
    protected void glEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }
    protected void glDisableScissor() {
        RenderSystem.disableScissor();
    }

    public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
        Matrix4f matrix4f = delegate.getMatrices().peek().getPositionMatrix();
        VertexConsumer vertexConsumer = delegate.getVertexConsumers().getBuffer(RenderLayer.getGui());
        quad.forEach(v -> vertexConsumer.vertex(matrix4f, v.x, v.y, z).color(color).next());
        delegate.draw();
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        delegate.setShaderColor(1, 1, 1, 1);
        delegate.drawNineSlicedTexture(ClickableWidget.WIDGETS_TEXTURE, x, y, width, height, 20, 4, 200, 20, 0, 46 + i * 20);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        float size = 20 / 64f;
        float u0 = focused ? size : 0, v0 = checked ? size : 0;
        float u1 = u0 + size, v1 = v0 + size;
        delegate.setShaderColor(1, 1, 1, 1);
        delegate.drawTexturedQuad(CHECKBOX_TEXTURE, x, x+width, y, y+height, 0, u0, u1, v0, v1);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        delegate.fill(RenderLayer.getGuiTextHighlight(), startX, startY, endX, endY, 0xff0000ff);
    }

    public void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float u2, float v1, float v2) {
        Identifier texture = ((ResourceLocationWrapperImpl) res).delegate();
        delegate.drawTexturedQuad(texture, x, x+w, y, y+h, 0, u1, u2, v1, v2);
    }

    public void drawWholeNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        Identifier texture = ((NativeTextureWrapperImpl) allocatedTextureObject).delegate;
        delegate.drawTexturedQuad(texture, x, x+w, y, y+h, 0, 0, 1, 0, 1);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Style style = ((StyleWrapperImpl) styleWrapper).delegate();
        delegate.drawHoverEvent(textRenderer, style, x, y);
    }

    public int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return delegate.drawTextWithShadow(textRenderer, string, (int) x, (int) y, color);
    }
}
