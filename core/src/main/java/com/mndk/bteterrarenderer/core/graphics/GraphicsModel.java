package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsTriangle;
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
    private final List<GraphicsQuad<?>> quads;
    private final List<GraphicsTriangle<?>> triangles;

    public GraphicsModel(Object textureObject, @Nonnull List<GraphicsShape<?>> shapes) {
        this.textureObject = textureObject;
        this.quads = new ArrayList<>();
        this.triangles = new ArrayList<>();

        for(GraphicsShape<?> shape : shapes) {
            if(shape instanceof GraphicsQuad) quads.add((GraphicsQuad<?>) shape);
            else if(shape instanceof GraphicsTriangle) triangles.add((GraphicsTriangle<?>) shape);
        }
    }
}
