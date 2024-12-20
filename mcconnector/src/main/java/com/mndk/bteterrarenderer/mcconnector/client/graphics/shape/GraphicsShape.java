package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordAABB;
import lombok.Getter;

@Getter
public abstract class GraphicsShape<T extends GraphicsVertex<T>> {
    private final Class<T> vertexClass;

    protected GraphicsShape(Class<T> vertexClass) {
        this.vertexClass = vertexClass;
    }

    public abstract T getVertex(int index);
    public abstract void setVertex(int index, T value);
    public abstract int getVerticesCount();
    public abstract McCoordAABB getBoundingBox();
}
