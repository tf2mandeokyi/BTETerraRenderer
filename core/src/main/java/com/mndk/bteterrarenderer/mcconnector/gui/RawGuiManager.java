package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;

import javax.annotation.Nullable;

public abstract class RawGuiManager {

    public static final RawGuiManager INSTANCE = makeInstance();
    private static RawGuiManager makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract void displayGuiScreen(AbstractGuiScreenCopy gui);

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
        return this.makeLine(x, y, x + dx, y + dy, thickness);
    }
}
