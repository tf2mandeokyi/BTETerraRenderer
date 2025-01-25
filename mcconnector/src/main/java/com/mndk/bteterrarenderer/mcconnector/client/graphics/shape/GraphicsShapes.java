package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.*;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.BTRUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicsShapes {
    private final Map<DrawingFormat<?>, List<GraphicsShape>> shapeMap = new HashMap<>();

    public <S extends GraphicsShape> void add(DrawingFormat<S> format, S shape) {
        List<S> list = BTRUtil.uncheckedCast(shapeMap.computeIfAbsent(format, key -> new ArrayList<>()));
        list.add(shape);
    }

    public void drawAndRender(WorldDrawContextWrapper context, NativeTextureWrapper texture,
                              McCoordTransformer modelPosTransformer, VertexBeginner beginner) {
        shapeMap.forEach((format, shapes) -> {
            BufferBuilderWrapper<?> builder = format.begin(beginner, texture);
            for (GraphicsShape shape : shapes) {
                builder.nextShape(context, BTRUtil.uncheckedCast(shape), modelPosTransformer);
            }
            builder.drawAndRender(context);
        });
    }
}
