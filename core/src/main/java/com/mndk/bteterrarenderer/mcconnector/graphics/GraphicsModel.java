package com.mndk.bteterrarenderer.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class GraphicsModel {
    private final Object textureObject;
    private final List<GraphicsQuad<?>> quads = new ArrayList<>();
    private final List<GraphicsTriangle<?>> triangles = new ArrayList<>();

    public GraphicsModel(Object textureObject, @Nonnull List<GraphicsShape<?>> shapes) {
        this.textureObject = textureObject;
        for(GraphicsShape<?> shape : shapes) {
            if(shape instanceof GraphicsQuad) quads.add((GraphicsQuad<?>) shape);
            else if(shape instanceof GraphicsTriangle) triangles.add((GraphicsTriangle<?>) shape);
        }
    }
}
