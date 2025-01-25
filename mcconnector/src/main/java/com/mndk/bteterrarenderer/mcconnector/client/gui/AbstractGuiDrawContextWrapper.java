package com.mndk.bteterrarenderer.mcconnector.client.gui;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;

public abstract class AbstractGuiDrawContextWrapper implements GuiDrawContextWrapper {

    private final Stack<int[]> scissorDimStack = new Stack<>();

    // GL scissor
    /**
     * Converts "relative" dimension to an absolute scissor dimension
     * @return {@code [ scissorX, scissorY, scissorWidth, scissorHeight ]}
     */
    protected abstract int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight);
    protected abstract void glEnableScissor(int x, int y, int width, int height);
    protected abstract void glDisableScissor();

    // GUI implementations
    public final void fillRect(int x1, int y1, int x2, int y2, int color) {
        GraphicsQuad<PosXY> quad = new GraphicsQuad<>(
                new PosXY(x1, y2),
                new PosXY(x2, y2),
                new PosXY(x2, y1),
                new PosXY(x1, y1)
        );
        this.fillQuad(quad, color, 0);
    }

    public final void drawWholeImage(ResourceLocationWrapper res, int x, int y, int w, int h) {
        this.drawImage(res, x, y, w, h, 0, 1, 0, 1);
    }

    public final void drawWholeCenteredImage(ResourceLocationWrapper res, int x, int y, int w, int h) {
        this.drawWholeImage(res, x - w/2, y - h/2, w, h);
    }

    // Font implementations
    public final void drawWidthSplitText(FontWrapper font, String str, int x, int y, int wrapWidth, int textColor) {
        List<String> splitLines = font.splitByWidth(str, wrapWidth);
        for (String line : splitLines) {
            this.drawTextWithShadow(font, line, x, y, textColor);
            y += font.getHeight();
        }
    }

    public final int drawTextWithShadow(FontWrapper fontWrapper, TextWrapper textWrapper, float x, float y, int color) {
        return textWrapper.drawWithShadow(fontWrapper, this, x, y, color);
    }

    public final void drawTextWithShadow(FontWrapper font, String text, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawTextWithShadow(font, text, x, y, color); break;
            case RIGHT:  this.drawTextWithShadow(font, text, x + width - font.getWidth(text), y, color); break;
            case CENTER: this.drawCenteredTextWithShadow(font, text, x + width / 2f, y, color); break;
        }
    }

    public final void drawTextWithShadow(FontWrapper font, String text, HorizontalAlign hAlign, VerticalAlign vAlign, float x, float y, int color) {
        String[] lines = text.split("\n");

        float left, top;
        int width = font.getWidth(text), height = font.getHeight() * lines.length;
        switch (hAlign) {
            case LEFT:   left = x; break;
            case CENTER: left = x - (float) width / 2; break;
            case RIGHT:  left = x - width; break;
            default:     throw new RuntimeException("Unknown align value");
        }
        switch (vAlign) {
            case TOP:    top = y; break;
            case MIDDLE: top = y - (float) height / 2; break;
            case BOTTOM: top = y - height; break;
            default:     throw new RuntimeException("Unknown align value");
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            this.drawTextWithShadow(font, line, left, top + i * font.getHeight(), color);
        }
    }

    public final void drawTextWithShadow(FontWrapper font, TextWrapper textComponent, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawTextWithShadow(font, textComponent, x, y, color); break;
            case RIGHT:  this.drawTextWithShadow(font, textComponent, x + width - font.getWidth(textComponent), y, color); break;
            case CENTER: this.drawCenteredTextWithShadow(font, textComponent, x + width / 2f, y, color); break;
        }
    }

    public final void drawCenteredTextWithShadow(FontWrapper font, String text, float x, float y, int color) {
        this.drawTextWithShadow(font, text, x - font.getWidth(text) / 2.0f, y, color);
    }

    public final void drawCenteredTextWithShadow(FontWrapper font, TextWrapper textComponent, float x, float y, int color) {
        this.drawTextWithShadow(font, textComponent, x - font.getWidth(textComponent) / 2.0f, y, color);
    }

    public final void glPushRelativeScissor(int relX, int relY, int relWidth, int relHeight) {
        int[] scissorDimension = this.getAbsoluteScissorDimension(relX, relY, relWidth, relHeight);
        scissorDimStack.push(scissorDimension);
        this.glUpdateScissorBox();
    }

    public final void glPopRelativeScissor() {
        if (!scissorDimStack.isEmpty()) scissorDimStack.pop();
        this.glUpdateScissorBox();
    }

    private void glUpdateScissorBox() {
        this.glDisableScissor();
        if (scissorDimStack.isEmpty()) return;

        // Calculate intersections
        int totalMinX = 0, totalMaxX = McConnector.client().getWindowSize().getPixelWidth();
        int totalMinY = 0, totalMaxY = McConnector.client().getWindowSize().getPixelHeight();
        for (int[] dimension : scissorDimStack) {
            int minX = dimension[0], maxX = dimension[0] + dimension[2];
            int minY = dimension[1], maxY = dimension[1] + dimension[3];
            if (totalMinX < minX) totalMinX = minX;
            if (totalMinY < minY) totalMinY = minY;
            if (totalMaxX > maxX) totalMaxX = maxX;
            if (totalMaxY > maxY) totalMaxY = maxY;
        }

        // Range validation
        if (totalMinX > totalMaxX) totalMaxX = totalMinX;
        if (totalMinY > totalMaxY) totalMaxY = totalMinY;

        // Do scissor
        int scissorX = totalMinX, scissorWidth = totalMaxX - totalMinX;
        int scissorY = totalMinY, scissorHeight = totalMaxY - totalMinY;
        this.glEnableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    @Nullable
    public final GraphicsQuad<PosXY> makeLine(double ax, double ay, double bx, double by, double thickness) {
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

        return new GraphicsQuad<>(
                new PosXY((float) x0, (float) y0),
                new PosXY((float) x1, (float) y1),
                new PosXY((float) x2, (float) y2),
                new PosXY((float) x3, (float) y3)
        );
    }

    @Nullable
    public final GraphicsQuad<PosXY> makeLineDxDy(double x, double y, double dx, double dy, double thickness) {
        return makeLine(x, y, x + dx, y + dy, thickness);
    }
}
