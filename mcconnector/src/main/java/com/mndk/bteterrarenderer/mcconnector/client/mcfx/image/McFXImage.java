package com.mndk.bteterrarenderer.mcconnector.client.mcfx.image;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;

import javax.annotation.Nullable;

public class McFXImage extends McFXElement {

    private NativeTextureWrapper texture = null;
    @Nullable private Integer imageWidth;
    @Nullable private Integer imageHeight;
    private int actualImageWidth;
    private int actualImageHeight;

    public McFXImage setTexture(NativeTextureWrapper texture) {
        this.texture = texture;
        this.updateDimension();
        return this;
    }

    public McFXImage setDimension(@Nullable Integer width, @Nullable Integer height) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.updateDimension();
        return this;
    }

    @Override
    protected void init() {
        this.updateDimension();
    }

    @Override
    protected void onWidthChange() {
        this.updateDimension();
    }

    @Override
    public int getPhysicalHeight() {
        return this.actualImageHeight;
    }

    @Override
    protected void drawElement(GuiDrawContextWrapper drawContextWrapper) {
        if (this.texture == null) return;
        drawContextWrapper.drawWholeNativeImage(this.texture, 0, 0, this.actualImageWidth, this.actualImageHeight);
    }

    private void updateDimension() {
        if (this.texture == null || this.texture.getWidth() == 0 || this.texture.getHeight() == 0) {
            this.actualImageWidth = 0;
            this.actualImageHeight = 0;
            return;
        }
        double ratio = (double) this.texture.getWidth() / this.texture.getHeight();
        if (this.imageWidth == null && this.imageHeight == null) {
            int elementWidth = this.getWidth();
            this.actualImageWidth = elementWidth;
            this.actualImageHeight = (int) (elementWidth / ratio);
        }
        else if (this.imageWidth == null) {
            this.actualImageWidth = (int) (this.imageHeight * ratio);
            this.actualImageHeight = this.imageHeight;
        }
        else if (this.imageHeight == null) {
            this.actualImageWidth = this.imageWidth;
            this.actualImageHeight = (int) (this.imageWidth / ratio);
        }
        else {
            this.actualImageWidth = this.imageWidth;
            this.actualImageHeight = this.imageHeight;
        }
    }
}
