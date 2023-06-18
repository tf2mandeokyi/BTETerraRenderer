package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.gui.components.GuiAbstractWidgetCopy;

public interface GuiStaticConnector {
    GuiStaticConnector INSTANCE = ImplFinder.search();

    void displayGuiScreen(AbstractGuiScreen gui);

    void fillQuad(Object poseStack, GraphicsQuad<GraphicsQuad.Pos> quad, int color);
    default void fillRect(Object poseStack, int x1, int y1, int x2, int y2, int color) {
        GraphicsQuad<GraphicsQuad.Pos> quad = new GraphicsQuad<>(
                new GraphicsQuad.Pos(x1, y2, 0),
                new GraphicsQuad.Pos(x2, y2, 0),
                new GraphicsQuad.Pos(x2, y1, 0),
                new GraphicsQuad.Pos(x1, y1, 0)
        );
        fillQuad(poseStack, quad, color);
    }
    default void drawLine(Object poseStack, double ax, double ay, double bx, double by, double thickness, int color) {

        if (ax == ay && bx == by) return;

        /*
         *  0-----------------------------1
         *  A  -  -  -  -  -  -  -  -  -  B
         *  3-----------------------------2
         */

        double deltaX = bx - ax, deltaY = by - ay, dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double dx = -deltaY * thickness / dist / 2, dy = deltaX * thickness / dist / 2;

        double x0 = ax - dx, y0 = ay - dy;
        double x1 = ax + dx, y1 = ay + dy;
        double x2 = bx + dx, y2 = by + dy;
        double x3 = bx - dx, y3 = by - dy;

        GraphicsQuad<GraphicsQuad.Pos> quad = new GraphicsQuad<>(
                new GraphicsQuad.Pos((float) x0, (float) y0, 0),
                new GraphicsQuad.Pos((float) x1, (float) y1, 0),
                new GraphicsQuad.Pos((float) x2, (float) y2, 0),
                new GraphicsQuad.Pos((float) x3, (float) y3, 0)
        );
        GuiStaticConnector.INSTANCE.fillQuad(poseStack, quad, color);
    }
    default void drawLineDxDy(Object poseStack, double x, double y, double dx, double dy, double thickness, int color) {
        drawLine(poseStack, x, y, x + dx, y + dy, thickness, color);
    }


    void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState);
    void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked);
    void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY);

    void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2);
    default void drawWholeImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h) {
        drawImage(poseStack, res, x, y, w, h, 0, 0, 1, 1);
    }
    default void drawWholeCenteredImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h) {
        drawWholeImage(poseStack, res, x - w/2, y - h/2, w, h);
    }
}
