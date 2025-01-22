package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.component.GuiEventListenerCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

public class GuiDrawContextWrapperImpl extends AbstractGuiDrawContextWrapper {

    public static final GuiDrawContextWrapperImpl INSTANCE = new GuiDrawContextWrapperImpl();
    private static final ResourceLocation WIDGET_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    public void translate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }
    public void pushMatrix() {
        GlStateManager.pushMatrix();
    }
    public void popMatrix() {
        GlStateManager.popMatrix();
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        float scaleFactorX = window.getScaleFactorX();
        float scaleFactorY = window.getScaleFactorY();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(buffer);

        Vector4f originalStart = new Vector4f(relX, relY, 0, 1);
        Vector4f originalEnd = new Vector4f(relX+relWidth, relY+relHeight, 0, 1);
        Vector4f start = Matrix4f.transform(matrix4f, originalStart, null);
        Vector4f end = Matrix4f.transform(matrix4f, originalEnd, null);

        int scissorX = (int) (scaleFactorX * Math.min(start.x, end.x));
        int scissorY = (int) (window.getPixelHeight() - scaleFactorY * Math.max(start.y, end.y));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.x - end.x));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.y - end.y));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }
    protected void glEnableScissor(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }
    protected void glDisableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(r, g, b, a);

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        quad.forEach(v -> bufferBuilder.pos(v.x, v.y, z).endVertex());
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        int i = 0;
        switch (hoverState) {
            case DISABLED:          break;
            case DEFAULT:    i = 1; break;
            case MOUSE_OVER: i = 2; break;
        }
        GuiUtils.drawContinuousTexturedBox(
                WIDGET_TEXTURES,
                x, y, 0, 46 + i * 20,
                width, height, 200, 20,
                2, 3, 2, 2,
                0
        );
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        drawButton(x, y, width, height, AbstractWidgetCopy.HoverState.DISABLED);
        if (checked) {
            FontWrapper font = McConnector.client().getDefaultFont();
            this.drawCenteredTextWithShadow(font, "x", x + width / 2f + 1, y + 1,
                    GuiEventListenerCopy.NORMAL_TEXT_COLOR);
        }
    }

    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);

        drawPosQuad(startX, startY, endX - startX, endY - startY);

        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h, float u0, float u1, float v0, float v1) {
        ResourceLocation resourceLocation = ((ResourceLocationWrapperImpl) res).delegate;
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        drawPosTexQuad(x, y, w, h, u0, v0, u1, v1);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) return;
        if (!(currentScreen instanceof AbstractGuiScreenImpl)) return;

        ((AbstractGuiScreenImpl) currentScreen).handleStyleHover(((StyleWrapperImpl) styleWrapper).delegate, x, y);
    }

    public void drawWholeNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.bindTexture(((NativeTextureWrapperImpl) allocatedTextureObject).delegate);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        drawPosTexQuad(x, y, w, h, 0, 0, 1, 1);
    }

    private static void drawPosQuad(float x, float y, float w, float h) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(x, y, 0).endVertex();
        bufferBuilder.pos(x, y+h, 0).endVertex();
        bufferBuilder.pos(x+w, y+h, 0).endVertex();
        bufferBuilder.pos(x+w, y, 0).endVertex();
        tessellator.draw();
    }

    private static void drawPosTexQuad(int x, int y, int w, int h, float u0, float v0, float u1, float v1) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(x, y, 0).tex(u0, v0).endVertex();
        bufferBuilder.pos(x, y+h, 0).tex(u0, v1).endVertex();
        bufferBuilder.pos(x+w, y+h, 0).tex(u1, v1).endVertex();
        bufferBuilder.pos(x+w, y, 0).tex(u1, v0).endVertex();
        tessellator.draw();
    }

    public int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color) {
        FontRenderer fontRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return fontRenderer.drawStringWithShadow(string, x, y, color);
    }
}
