package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.VerticalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;

public abstract class DrawContextWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    protected DrawContextWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    // Buffer builder
    public abstract BufferBuilderWrapper<?> tessellatorBufferBuilder();

    // Transformation
    public abstract void translate(float x, float y, float z);
    public abstract void pushMatrix();
    public abstract void popMatrix();

    // GL scissor
    /**
     * Converts "relative" dimension to an absolute scissor dimension
     * @return {@code [ scissorX, scissorY, scissorWidth, scissorHeight ]}
     */
    protected abstract int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight);

    // GUI
    public abstract void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState);
    public abstract void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked);
    public abstract void drawTextHighlight(int startX, int startY, int endX, int endY);
    public abstract void drawImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h, float u1, float v1, float u2, float v2);
    public abstract void drawHoverEvent(StyleWrapper styleWrapper, int x, int y);

    // Font
    public abstract int drawTextWithShadow(FontWrapper<?> fontWrapper, String string, float x, float y, int color);
    public abstract int drawTextWithShadow(FontWrapper<?> fontWrapper, TextWrapper textWrapper, float x, float y, int color);

    // GUI implementations
    public void fillRect(int x1, int y1, int x2, int y2, int color) {
        GraphicsQuad<PosXY> quad = GraphicsQuad.newPosXY(
                new PosXY(x1, y2),
                new PosXY(x2, y2),
                new PosXY(x2, y1),
                new PosXY(x1, y1)
        );
        this.fillQuad(quad, color, 0);
    }

    public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
        PosXY v0 = quad.getVertex(0);
        PosXY v1 = quad.getVertex(1);
        PosXY v2 = quad.getVertex(2);
        PosXY v3 = quad.getVertex(3);

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        McConnector.client().glGraphicsManager.glEnableBlend();
        McConnector.client().glGraphicsManager.glDisableTexture();
        McConnector.client().glGraphicsManager.glDefaultBlendFunc();
        McConnector.client().glGraphicsManager.setPositionColorShader();
        BufferBuilderWrapper<?> bufferBuilder = this.tessellatorBufferBuilder();
        bufferBuilder.beginPcQuads();
        bufferBuilder.pcNext(this, v0.x, v0.y, z, r, g, b, a);
        bufferBuilder.pcNext(this, v1.x, v1.y, z, r, g, b, a);
        bufferBuilder.pcNext(this, v2.x, v2.y, z, r, g, b, a);
        bufferBuilder.pcNext(this, v3.x, v3.y, z, r, g, b, a);
        bufferBuilder.drawAndRender();
        McConnector.client().glGraphicsManager.glEnableTexture();
        McConnector.client().glGraphicsManager.glDisableBlend();
    }

    protected void drawPosQuad(float x, float y, float w, float h) {
        BufferBuilderWrapper<?> bufferBuilder = this.tessellatorBufferBuilder();
        bufferBuilder.beginPQuads();
        bufferBuilder.pNext(this, x, y, 0);
        bufferBuilder.pNext(this, x, y+h, 0);
        bufferBuilder.pNext(this, x+w, y+h, 0);
        bufferBuilder.pNext(this, x+w, y, 0);
        bufferBuilder.drawAndRender();
    }

    protected void drawPosTexQuad(int x, int y, int w, int h,
                                  float u0, float v0, float u1, float v1) {
        BufferBuilderWrapper<?> bufferBuilder = this.tessellatorBufferBuilder();
        bufferBuilder.beginPtQuads();
        bufferBuilder.ptNext(this, x, y, 0, u0, v0);
        bufferBuilder.ptNext(this, x, y+h, 0, u0, v1);
        bufferBuilder.ptNext(this, x+w, y+h, 0, u1, v1);
        bufferBuilder.ptNext(this, x+w, y, 0, u1, v0);
        bufferBuilder.drawAndRender();
    }

    public void drawNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        McConnector.client().glGraphicsManager.setPositionTexShader();
        McConnector.client().glGraphicsManager.setShaderTexture(allocatedTextureObject);
        McConnector.client().glGraphicsManager.glEnableBlend();
        McConnector.client().glGraphicsManager.glDefaultBlendFunc();
        this.drawPosTexQuad(x, y, w, h, 0, 0, 1, 1);
    }

    public void drawWholeImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h) {
        this.drawImage(res, x, y, w, h, 0, 0, 1, 1);
    }

    public void drawWholeCenteredImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h) {
        this.drawWholeImage(res, x - w/2, y - h/2, w, h);
    }

    // Font implementations
    public void drawWidthSplitText(FontWrapper<?> font, String str, int x, int y, int wrapWidth, int textColor) {
        List<String> splitLines = font.splitByWidth(str, wrapWidth);
        for (String line : splitLines) {
            this.drawTextWithShadow(font, line, x, y, textColor);
            y += font.getHeight();
        }
    }

    public void drawTextWithShadow(FontWrapper<?> font, String text, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawTextWithShadow(font, text, x, y, color); break;
            case RIGHT:  this.drawTextWithShadow(font, text, x + width - font.getWidth(text), y, color); break;
            case CENTER: this.drawCenteredTextWithShadow(font, text, x + width / 2f, y, color); break;
        }
    }

    public void drawTextWithShadow(FontWrapper<?> font, String text, HorizontalAlign hAlign, VerticalAlign vAlign, float x, float y, int color) {
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

    public void drawTextWithShadow(FontWrapper<?> font, TextWrapper textComponent, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawTextWithShadow(font, textComponent, x, y, color); break;
            case RIGHT:  this.drawTextWithShadow(font, textComponent, x + width - font.getWidth(textComponent), y, color); break;
            case CENTER: this.drawCenteredTextWithShadow(font, textComponent, x + width / 2f, y, color); break;
        }
    }

    public void drawCenteredTextWithShadow(FontWrapper<?> font, String text, float x, float y, int color) {
        this.drawTextWithShadow(font, text, x - font.getWidth(text) / 2.0f, y, color);
    }

    public void drawCenteredTextWithShadow(FontWrapper<?> font, TextWrapper textComponent, float x, float y, int color) {
        this.drawTextWithShadow(font, textComponent, x - font.getWidth(textComponent) / 2.0f, y, color);
    }

    // GL scissor
    private final Stack<int[]> SCISSOR_DIM_STACK = new Stack<>();

    public void glPushRelativeScissor(int relX, int relY, int relWidth, int relHeight) {
        int[] scissorDimension = this.getAbsoluteScissorDimension(relX, relY, relWidth, relHeight);
        SCISSOR_DIM_STACK.push(scissorDimension);
        this.glUpdateScissorBox();
    }

    public void glPopRelativeScissor() {
        if (!SCISSOR_DIM_STACK.isEmpty()) SCISSOR_DIM_STACK.pop();
        this.glUpdateScissorBox();
    }

    private void glUpdateScissorBox() {
        McConnector.client().glGraphicsManager.glDisableScissorTest();
        if (SCISSOR_DIM_STACK.isEmpty()) return;

        // Calculate intersections
        int totalMinX = 0, totalMaxX = McConnector.client().getWindowSize().getPixelWidth();
        int totalMinY = 0, totalMaxY = McConnector.client().getWindowSize().getPixelHeight();
        for (int[] dimension : SCISSOR_DIM_STACK) {
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
        McConnector.client().glGraphicsManager.glEnableScissorTest();
        McConnector.client().glGraphicsManager.glScissorBox(scissorX, scissorY, scissorWidth, scissorHeight);
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
}
