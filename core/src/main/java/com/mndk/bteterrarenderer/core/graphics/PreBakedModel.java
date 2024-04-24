package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public class PreBakedModel {
    private final BufferedImage image;
    private final GraphicsShapes shapes;
}
