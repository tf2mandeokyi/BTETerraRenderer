package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class PreBakedModel {
    private final BufferedImage image;
    private final List<GraphicsQuad<?>> quads = new ArrayList<>();
    private final List<GraphicsTriangle<?>> triangles = new ArrayList<>();

    public PreBakedModel(BufferedImage image, List<GraphicsShape<?>> shapes) {
        this.image = image;
        for(GraphicsShape<?> shape : shapes) {
            if(shape instanceof GraphicsQuad) quads.add((GraphicsQuad<?>) shape);
            else if(shape instanceof GraphicsTriangle) triangles.add((GraphicsTriangle<?>) shape);
        }
    }

    public PreBakedModel(BufferedImage image, GraphicsShape<?> shape) {
        this(image, Collections.singletonList(shape));
    }
}
