package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawModeEnum;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.VertexFormatEnum;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.VerticalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;

import javax.annotation.Nullable;

public interface DrawContextWrapper {

    BufferBuilderWrapper begin(DrawModeEnum glMode, VertexFormatEnum vertexFormat);

    // Transformation
    void translate(float x, float y, float z);
    void pushMatrix();
    void popMatrix();

    // GUI
    void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState);
    void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked);
    void drawTextHighlight(int startX, int startY, int endX, int endY);
    void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float v1, float u2, float v2);
    void drawHoverEvent(StyleWrapper styleWrapper, int x, int y);

    // Font
    int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color);
    int drawTextWithShadow(FontWrapper fontWrapper, TextWrapper textWrapper, float x, float y, int color);

    // GUI implementations
    void fillRect(int x1, int y1, int x2, int y2, int color);
    void fillQuad(GraphicsQuad<PosXY> quad, int color, float z);
    void drawPosQuad(float x, float y, float w, float h);
    void drawPosTexQuad(int x, int y, int w, int h, float u0, float v0, float u1, float v1);
    void drawNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h);
    void drawWholeImage(ResourceLocationWrapper res, int x, int y, int w, int h);
    void drawWholeCenteredImage(ResourceLocationWrapper res, int x, int y, int w, int h);

    // Font implementations
    void drawWidthSplitText(FontWrapper font, String str, int x, int y, int wrapWidth, int textColor);
    void drawTextWithShadow(FontWrapper font, String text, HorizontalAlign align, float x, float y, float width, int color);
    void drawTextWithShadow(FontWrapper font, String text, HorizontalAlign hAlign, VerticalAlign vAlign, float x, float y, int color);
    void drawTextWithShadow(FontWrapper font, TextWrapper textComponent, HorizontalAlign align, float x, float y, float width, int color);
    void drawCenteredTextWithShadow(FontWrapper font, String text, float x, float y, int color);
    void drawCenteredTextWithShadow(FontWrapper font, TextWrapper textComponent, float x, float y, int color);

    // GL scissor
    void glPushRelativeScissor(int relX, int relY, int relWidth, int relHeight);
    void glPopRelativeScissor();

    // Line drawing
    @Nullable GraphicsQuad<PosXY> makeLine(double ax, double ay, double bx, double by, double thickness);
    @Nullable GraphicsQuad<PosXY> makeLineDxDy(double x, double y, double dx, double dy, double thickness);
}
