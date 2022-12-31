package com.mndk.bteterrarenderer.util.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

public class GuiUtils {

    public static void drawLine(double ax, double ay, double bx, double by, double thickness, int color) {

        if(ax == ay && bx == by) return;

        /*
         *  0-----------------------------------------1
         *  |                                         |
         *  A  -  -  -  -  -  -  -  -  -  -  -  -  -  B
         *  |                                         |
         *  3-----------------------------------------2
         */

        double deltaX = bx - ax, deltaY = by - ay, dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double dx = -deltaY * thickness / dist / 2, dy = deltaX * thickness / dist / 2;

        double x0 = ax - dx, y0 = ay - dy;
        double x1 = ax + dx, y1 = ay + dy;
        double x2 = bx + dx, y2 = by + dy;
        double x3 = bx - dx, y3 = by - dy;

        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x2, y2, 0.0D).endVertex();
        bufferbuilder.pos(x3, y3, 0.0D).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    public static void drawLineDxDy(double x, double y, double dx, double dy, double thickness, int color) {
        drawLine(x, y, x + dx, y + dy, thickness, color);
    }


    public static void glRelativeScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.rewind();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(buffer);

        int translateX = (int) matrix4f.m30, translateY = (int) matrix4f.m31;
        int scaleFactor = scaledResolution.getScaleFactor();
        GL11.glScissor(
                scaleFactor * (x + translateX), mc.displayHeight - scaleFactor * (y + translateY + height),
                scaleFactor * width, scaleFactor * height
        );
    }


    public static void drawImage(ResourceLocation res, int x, int y, float zLevel, int w, int h, float u1, float v1, float u2, float v2) {

        if(res != null) Minecraft.getMinecraft().renderEngine.bindTexture(res);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y+h, zLevel).tex(u1, v2).endVertex();
        bufferbuilder.pos(x+w, y+h, zLevel).tex(u2, v2).endVertex();
        bufferbuilder.pos(x+w, y, zLevel).tex(u2, v1).endVertex();
        bufferbuilder.pos(x, y, zLevel).tex(u1, v1).endVertex();

        tessellator.draw();
    }


    public static void drawImage(ResourceLocation res, int x, int y, float zLevel, int w, int h) {
        drawImage(res, x, y, zLevel, w, h, 0, 0, 1, 1);
    }


    public static void drawCenteredImage(ResourceLocation res, int x, int y, float zLevel, int w, int h) {
        drawImage(res, x - w/2, y - h/2, zLevel, w, h);
    }
}
