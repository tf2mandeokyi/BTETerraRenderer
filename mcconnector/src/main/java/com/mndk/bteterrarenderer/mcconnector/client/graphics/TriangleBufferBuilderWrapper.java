package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.Getter;

@Getter
public abstract class TriangleBufferBuilderWrapper<V extends GraphicsVertex> implements BufferBuilderWrapper<GraphicsTriangle<V>> {
    // late init
    private McCoordTransformer transformer = null;

    @Override
    public final void setTransformer(McCoordTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public final void nextShape(GraphicsTriangle<V> shape) {
        this.next(shape.v0);
        this.next(shape.v1);
        this.next(shape.v2);
    }

    protected abstract void next(V vertex);
}
