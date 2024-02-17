package com.mndk.bteterrarenderer.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftWindowManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.VerticalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.AbstractWidgetCopy;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Stack;

public abstract class DrawContextWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    public static DrawContextWrapper<?> of(Object delegate) {
        return MixinUtil.notOverwritten(delegate);
    }

    protected DrawContextWrapper(@Nonnull Object delegate) {
        super(delegate);
    }

    // Transformation
    public abstract void translate(float x, float y, float z);
    public abstract void pushMatrix();
    public abstract void popMatrix();

    // GL scissor
    /**
     * Converts "relative" dimension to an absolute scissor dimension
     * @return {@code [ scissorX, scissorY, scissorWidth, scissorHeight ]}
     */
    public abstract int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight);

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

        GlGraphicsManager.INSTANCE.glEnableBlend();
        GlGraphicsManager.INSTANCE.glDisableTexture();
        GlGraphicsManager.INSTANCE.glDefaultBlendFunc();
        GlGraphicsManager.INSTANCE.setPositionColorShader();
        IBufferBuilder bufferBuilder = IBufferBuilder.getTessellatorInstance();
        bufferBuilder.beginPCQuads();
        bufferBuilder.pc(this, v0.x, v0.y, z, r, g, b, a);
        bufferBuilder.pc(this, v1.x, v1.y, z, r, g, b, a);
        bufferBuilder.pc(this, v2.x, v2.y, z, r, g, b, a);
        bufferBuilder.pc(this, v3.x, v3.y, z, r, g, b, a);
        bufferBuilder.drawAndRender();
        GlGraphicsManager.INSTANCE.glEnableTexture();
        GlGraphicsManager.INSTANCE.glDisableBlend();
    }

    protected void drawPosQuad(float x, float y, float w, float h) {
        IBufferBuilder bufferBuilder = IBufferBuilder.getTessellatorInstance();
        bufferBuilder.beginPQuads();
        bufferBuilder.p(this, x, y, 0);
        bufferBuilder.p(this, x, y+h, 0);
        bufferBuilder.p(this, x+w, y+h, 0);
        bufferBuilder.p(this, x+w, y, 0);
        bufferBuilder.drawAndRender();
    }

    protected void drawPosTexQuad(int x, int y, int w, int h,
                                  float u0, float v0, float u1, float v1) {
        IBufferBuilder bufferBuilder = IBufferBuilder.getTessellatorInstance();
        bufferBuilder.beginPTQuads();
        bufferBuilder.pt(this, x, y, 0, u0, v0);
        bufferBuilder.pt(this, x, y+h, 0, u0, v1);
        bufferBuilder.pt(this, x+w, y+h, 0, u1, v1);
        bufferBuilder.pt(this, x+w, y, 0, u1, v0);
        bufferBuilder.drawAndRender();
    }

    public void drawNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        GlGraphicsManager.INSTANCE.setPositionTexShader();
        GlGraphicsManager.INSTANCE.setShaderTexture(allocatedTextureObject);
        GlGraphicsManager.INSTANCE.glEnableBlend();
        GlGraphicsManager.INSTANCE.glDefaultBlendFunc();
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
        for(String line : splitLines) {
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
        switch(hAlign) {
            case LEFT:   left = x; break;
            case CENTER: left = x - (float) width / 2; break;
            case RIGHT:  left = x - width; break;
            default:     throw new RuntimeException("Unknown align value");
        }
        switch(vAlign) {
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
        if(!SCISSOR_DIM_STACK.isEmpty()) SCISSOR_DIM_STACK.pop();
        this.glUpdateScissorBox();
    }

    private void glUpdateScissorBox() {
        GlGraphicsManager.INSTANCE.glDisableScissorTest();
        if(SCISSOR_DIM_STACK.isEmpty()) return;

        // Calculate intersections
        int totalMinX = 0, totalMaxX = MinecraftWindowManager.INSTANCE.getPixelWidth();
        int totalMinY = 0, totalMaxY = MinecraftWindowManager.INSTANCE.getPixelHeight();
        for(int[] dimension : SCISSOR_DIM_STACK) {
            int minX = dimension[0], maxX = dimension[0] + dimension[2];
            int minY = dimension[1], maxY = dimension[1] + dimension[3];
            if(totalMinX < minX) totalMinX = minX;
            if(totalMinY < minY) totalMinY = minY;
            if(totalMaxX > maxX) totalMaxX = maxX;
            if(totalMaxY > maxY) totalMaxY = maxY;
        }

        // Range validation
        if(totalMinX > totalMaxX) totalMaxX = totalMinX;
        if(totalMinY > totalMaxY) totalMaxY = totalMinY;

        // Do scissor
        int scissorX = totalMinX, scissorWidth = totalMaxX - totalMinX;
        int scissorY = totalMinY, scissorHeight = totalMaxY - totalMinY;
        GlGraphicsManager.INSTANCE.glEnableScissorTest();
        GlGraphicsManager.INSTANCE.glScissorBox(scissorX, scissorY, scissorWidth, scissorHeight);
    }
}
