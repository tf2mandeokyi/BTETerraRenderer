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
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class GuiDrawContextWrapperImpl extends AbstractGuiDrawContextWrapper {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = Identifier.of("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED = Identifier.of("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED = Identifier.of("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX = Identifier.of("widget/checkbox");
    private static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of("widget/button"),
            Identifier.of("widget/button_disabled"),
            Identifier.of("widget/button_highlighted")
    );

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
        quad.forEach(v -> vertexConsumer.vertex(matrix4f, v.x, v.y, z).color(color));
        delegate.draw();
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        boolean enabled = hoverState != AbstractWidgetCopy.HoverState.DISABLED;
        boolean focused = hoverState == AbstractWidgetCopy.HoverState.MOUSE_OVER;
        Identifier buttonTexture = BUTTON_TEXTURES.get(enabled, focused);
        delegate.drawGuiTexture(buttonTexture, x, y, width, height);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Identifier identifier = checked ?
                (focused ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED) :
                (focused ? CHECKBOX_HIGHLIGHTED : CHECKBOX);
        delegate.setShaderColor(1, 1, 1, 1);
        delegate.drawGuiTexture(identifier, x, y, width, height);
    }

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
