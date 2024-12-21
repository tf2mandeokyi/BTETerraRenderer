package com.mndk.bteterrarenderer.mcconnector.client.graphics.format;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

@FunctionalInterface
public
interface VertexBeginner {
    BufferBuilderWrapper<?> begin(DrawContextWrapper<?> drawContextWrapper);
}
