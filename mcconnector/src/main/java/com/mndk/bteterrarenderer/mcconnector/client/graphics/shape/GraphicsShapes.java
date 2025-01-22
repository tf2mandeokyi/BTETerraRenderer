package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.BTRUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicsShapes {
    private final Map<DrawingFormat<?>, List<GraphicsShape>> shapeMap = new HashMap<>();

    public <U extends GraphicsShape> void add(DrawingFormat<U> format, U shape) {
        List<U> list = BTRUtil.uncheckedCast(shapeMap.computeIfAbsent(format, key -> new ArrayList<>()));
        list.add(shape);
    }

    public void drawAndRender(WorldDrawContextWrapper context, NativeTextureWrapper texture,
                              McCoordTransformer modelPosTransformer, float alpha) {
        shapeMap.forEach((format, shapes) -> {
            BufferBuilderWrapper<?> builder = format.begin(texture);
            for (GraphicsShape shape : shapes) {
                builder.nextShape(context, BTRUtil.uncheckedCast(shape), modelPosTransformer, alpha);
            }
            builder.drawAndRender(context);
        });
    }
}
