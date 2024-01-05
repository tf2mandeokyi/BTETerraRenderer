package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

@Data
public class PreBakedModel {
    private final BufferedImage image;
    private final List<GraphicsShape<?>> shapes;

    public PreBakedModel(BufferedImage image, List<GraphicsShape<?>> shapes) {
        this.image = image;
        this.shapes = shapes;
    }

    public PreBakedModel(BufferedImage image, GraphicsShape<?> shape) {
        this.image = image;
        this.shapes = Collections.singletonList(shape);
    }
}
