package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.List;

@Data
public class PreBakedModel {
    private final BufferedImage image;
    private final List<GraphicsShape<?>> shapes;
}
