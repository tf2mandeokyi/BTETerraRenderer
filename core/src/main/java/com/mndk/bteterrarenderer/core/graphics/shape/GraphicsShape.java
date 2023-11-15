package com.mndk.bteterrarenderer.core.graphics.shape;

import com.mndk.bteterrarenderer.core.graphics.format.VertexInfo;
import lombok.Getter;

@Getter
public abstract class GraphicsShape<T extends VertexInfo> {
    private final Class<T> vertexClass;

    protected GraphicsShape(Class<T> vertexClass) {
        this.vertexClass = vertexClass;
    }

    public abstract T getVertex(int index);
    public abstract void setVertex(int index, T value);
    public abstract int getVerticesCount();
}
