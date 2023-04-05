package com.mndk.bteterrarenderer.util.gui;

import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.connector.graphics.GlFactor;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.VertexFormatConnectorEnum;

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

        IBufferBuilder bufferbuilder = GraphicsConnector.INSTANCE.getBufferBuilder();
        GraphicsConnector.INSTANCE.glEnableBlend();
        GraphicsConnector.INSTANCE.glDisableTexture2D();
        GraphicsConnector.INSTANCE.glTryBlendFuncSeparate(GlFactor.SRC_ALPHA, GlFactor.ONE_MINUS_SRC_ALPHA, GlFactor.ONE, GlFactor.ZERO);
        GraphicsConnector.INSTANCE.glColor(red, green, blue, alpha);

        bufferbuilder.beginQuads(VertexFormatConnectorEnum.POSITION);
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x2, y2, 0.0D).endVertex();
        bufferbuilder.pos(x3, y3, 0.0D).endVertex();

        GraphicsConnector.INSTANCE.tessellatorDraw();
        GraphicsConnector.INSTANCE.glEnableTexture2D();
        GraphicsConnector.INSTANCE.glDisableBlend();

    }

    public static void drawLineDxDy(double x, double y, double dx, double dy, double thickness, int color) {
        drawLine(x, y, x + dx, y + dy, thickness, color);
    }


    public static void drawImage(IResourceLocation res, int x, int y, float zLevel, int w, int h, float u1, float v1, float u2, float v2) {

        if(res != null) GraphicsConnector.INSTANCE.bindTexture(res);

        IBufferBuilder bufferBuilder = GraphicsConnector.INSTANCE.getBufferBuilder();

        bufferBuilder.beginQuads(VertexFormatConnectorEnum.POSITION_TEX);
        bufferBuilder.pos(x, y+h, zLevel).tex(u1, v2).endVertex();
        bufferBuilder.pos(x+w, y+h, zLevel).tex(u2, v2).endVertex();
        bufferBuilder.pos(x+w, y, zLevel).tex(u2, v1).endVertex();
        bufferBuilder.pos(x, y, zLevel).tex(u1, v1).endVertex();

        GraphicsConnector.INSTANCE.tessellatorDraw();
    }


    public static void drawImage(IResourceLocation res, int x, int y, float zLevel, int w, int h) {
        drawImage(res, x, y, zLevel, w, h, 0, 0, 1, 1);
    }


    public static void drawCenteredImage(IResourceLocation res, int x, int y, float zLevel, int w, int h) {
        drawImage(res, x - w/2, y - h/2, zLevel, w, h);
    }
}
