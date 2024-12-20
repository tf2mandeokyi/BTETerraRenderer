package com.mndk.bteterrarenderer.mcconnector.client;

public interface WindowDimension {
    int getPixelWidth();
    int getPixelHeight();
    int getScaledWidth();
    int getScaledHeight();

    default float getScaleFactorX() {
        return (float) this.getPixelWidth() / this.getScaledWidth();
    }
    default float getScaleFactorY() {
        return (float) this.getPixelHeight() / this.getScaledHeight();
    }
}
