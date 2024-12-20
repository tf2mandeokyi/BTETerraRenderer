package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;

@FunctionalInterface
public
interface VertexBeginner {
    void begin(BufferBuilderWrapper<?> builder);
}
