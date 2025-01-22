package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class GraphicsQuad<T extends GraphicsVertex> implements GraphicsShape {

    public final T v0, v1, v2, v3;

    public void forEach(Consumer<T> consumer) {
        consumer.accept(this.v0);
        consumer.accept(this.v1);
        consumer.accept(this.v2);
        consumer.accept(this.v3);
    }

    @Override
    public String toString() {
        return String.format("GraphicsQuad[%s, %s, %s, %s]", v0, v1, v2, v3);
    }
}
