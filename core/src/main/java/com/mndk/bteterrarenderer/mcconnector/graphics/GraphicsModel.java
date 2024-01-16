package com.mndk.bteterrarenderer.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class GraphicsModel {
    private final NativeTextureWrapper textureObject;
    private final List<GraphicsQuad<?>> quads;
    private final List<GraphicsTriangle<?>> triangles;

    public GraphicsModel(NativeTextureWrapper textureObject, @Nonnull List<GraphicsShape<?>> shapes) {
        this(textureObject, new ArrayList<>(), new ArrayList<>());
        for(GraphicsShape<?> shape : shapes) {
            if(shape instanceof GraphicsQuad) quads.add((GraphicsQuad<?>) shape);
            else if(shape instanceof GraphicsTriangle) triangles.add((GraphicsTriangle<?>) shape);
        }
    }
}
