package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.Getter;

@Getter
public abstract class QuadBufferBuilderWrapper<V extends GraphicsVertex> implements BufferBuilderWrapper<GraphicsQuad<V>> {
    // late init
    private McCoordTransformer transformer = null;

    @Override
    public final void setTransformer(McCoordTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public final void nextShape(GraphicsQuad<V> shape) {
        this.next(shape.v0);
        this.next(shape.v1);
        this.next(shape.v2);
        this.next(shape.v3);
    }

    protected abstract void next(V vertex);
}
