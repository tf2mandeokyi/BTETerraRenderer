package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.graphics.format.PosXY;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.gui.components.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
public class RawGuiManager {

    public void displayGuiScreen(AbstractGuiScreenCopy gui) {
        MixinUtil.notOverwritten(gui);
    }

    public void fillQuad(Object poseStack, GraphicsQuad<PosXY> shape, int color, float z) {
        MixinUtil.notOverwritten(poseStack, shape, color, z);
    }
    public void fillRect(Object poseStack, int x1, int y1, int x2, int y2, int color) {
        GraphicsQuad<PosXY> quad = GraphicsQuad.newPosXY(
                new PosXY(x1, y2),
                new PosXY(x2, y2),
                new PosXY(x2, y1),
                new PosXY(x1, y1)
        );
        fillQuad(poseStack, quad, color, 0);
    }

    @Nullable
    public GraphicsQuad<PosXY> makeLine(double ax, double ay, double bx, double by, double thickness) {
        if (ax == ay && bx == by) return null;

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

        return GraphicsQuad.newPosXY(
                new PosXY((float) x0, (float) y0),
                new PosXY((float) x1, (float) y1),
                new PosXY((float) x2, (float) y2),
                new PosXY((float) x3, (float) y3)
        );
    }

    @Nullable
    public GraphicsQuad<PosXY> makeLineDxDy(double x, double y, double dx, double dy, double thickness) {
        return makeLine(x, y, x + dx, y + dy, thickness);
    }

    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
        MixinUtil.notOverwritten(poseStack, x, y, width, height, hoverState);
    }
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        MixinUtil.notOverwritten(poseStack, x, y, width, height, focused, checked);
    }
    public void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY) {
        MixinUtil.notOverwritten(poseStack, startX, startY, endX, endY);
    }

    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        MixinUtil.notOverwritten(poseStack, res, x, y, w, h, u1, v1, u2, v2);
    }
    public void drawWholeImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h) {
        drawImage(poseStack, res, x, y, w, h, 0, 0, 1, 1);
    }
    public void drawWholeCenteredImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h) {
        drawWholeImage(poseStack, res, x - w/2, y - h/2, w, h);
    }

    public void drawNativeImage(Object poseStack, Object allocatedTextureObject, int x, int y, int w, int h) {
        MixinUtil.notOverwritten(poseStack, allocatedTextureObject, x, y, w, h);
    }
}
