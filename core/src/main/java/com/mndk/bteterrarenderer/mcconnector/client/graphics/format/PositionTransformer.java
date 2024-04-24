package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

public interface PositionTransformer {
    double[] transform(double x, double y, double z);
}
