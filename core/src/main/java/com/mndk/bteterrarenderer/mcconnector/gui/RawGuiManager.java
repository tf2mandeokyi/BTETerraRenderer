package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.ResourceLocationWrapper;

import javax.annotation.Nullable;

public abstract class RawGuiManager {

    public static final RawGuiManager INSTANCE = makeInstance();
    private static RawGuiManager makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract void displayGuiScreen(AbstractGuiScreenCopy gui);
    public abstract void drawButton(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState);
    public abstract void drawCheckBox(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, boolean focused, boolean checked);
    public abstract void drawTextFieldHighlight(DrawContextWrapper drawContextWrapper, int startX, int startY, int endX, int endY);
    public abstract void drawImage(DrawContextWrapper drawContextWrapper, ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float v1, float u2, float v2);

    public void fillRect(DrawContextWrapper drawContextWrapper, int x1, int y1, int x2, int y2, int color) {
        GraphicsQuad<PosXY> quad = GraphicsQuad.newPosXY(
                new PosXY(x1, y2),
                new PosXY(x2, y2),
                new PosXY(x2, y1),
                new PosXY(x1, y1)
        );
        this.fillQuad(drawContextWrapper, quad, color, 0);
    }

    public void fillQuad(DrawContextWrapper drawContextWrapper, GraphicsQuad<PosXY> quad, int color, float z) {
        PosXY v0 = quad.getVertex(0);
        PosXY v1 = quad.getVertex(1);
        PosXY v2 = quad.getVertex(2);
        PosXY v3 = quad.getVertex(3);

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        GlGraphicsManager.INSTANCE.glEnableBlend();
        GlGraphicsManager.INSTANCE.glDisableTexture();
        GlGraphicsManager.INSTANCE.glDefaultBlendFunc();
        GlGraphicsManager.INSTANCE.setPositionColorShader();
        IBufferBuilder bufferBuilder = IBufferBuilder.getTessellatorInstance();
        bufferBuilder.beginPCQuads();
        bufferBuilder.pc(drawContextWrapper, v0.x, v0.y, z, r, g, b, a);
        bufferBuilder.pc(drawContextWrapper, v1.x, v1.y, z, r, g, b, a);
        bufferBuilder.pc(drawContextWrapper, v2.x, v2.y, z, r, g, b, a);
        bufferBuilder.pc(drawContextWrapper, v3.x, v3.y, z, r, g, b, a);
        bufferBuilder.drawAndRender();
        GlGraphicsManager.INSTANCE.glEnableTexture();
        GlGraphicsManager.INSTANCE.glDisableBlend();
    }

    public void drawNativeImage(DrawContextWrapper drawContextWrapper, Object allocatedTextureObject, int x, int y, int w, int h) {
        GlGraphicsManager.INSTANCE.setPositionTexShader();
        GlGraphicsManager.INSTANCE.setShaderTexture(allocatedTextureObject);
        GlGraphicsManager.INSTANCE.glEnableBlend();
        GlGraphicsManager.INSTANCE.glDefaultBlendFunc();
        IBufferBuilder bufferBuilder = IBufferBuilder.getTessellatorInstance();
        bufferBuilder.beginPTQuads();
        bufferBuilder.pt(drawContextWrapper, x, y, 0, 0, 0);
        bufferBuilder.pt(drawContextWrapper, x, y+h, 0, 0, 1);
        bufferBuilder.pt(drawContextWrapper, x+w, y+h, 0, 1, 1);
        bufferBuilder.pt(drawContextWrapper, x+w, y, 0, 1, 0);
        bufferBuilder.drawAndRender();
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
        return this.makeLine(x, y, x + dx, y + dy, thickness);
    }

    public void drawWholeImage(DrawContextWrapper drawContextWrapper, ResourceLocationWrapper res, int x, int y, int w, int h) {
        this.drawImage(drawContextWrapper, res, x, y, w, h, 0, 0, 1, 1);
    }

    public void drawWholeCenteredImage(DrawContextWrapper drawContextWrapper, ResourceLocationWrapper res, int x, int y, int w, int h) {
        this.drawWholeImage(drawContextWrapper, res, x - w/2, y - h/2, w, h);
    }
}
