package com.mndk.bteterrarenderer.core.graphics;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.List;

@Data
public class PreBakedModel {
    private final BufferedImage image;
    private final List<GraphicsQuad<?>> quads;
}
