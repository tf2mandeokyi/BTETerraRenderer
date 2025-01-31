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
            builder.setContext(context);
            builder.setTransformer(modelPosTransformer);
            // Even though the "RenderLayer"s or "RenderType"s do not require to
            // be pre-configured after 1.18.2, we still need to call preUpload()
            // since 1.12.2 requires it.
            builder.preUpload();
            for (GraphicsShape shape : shapes) {
                builder.nextShape(BTRUtil.uncheckedCast(shape));
            }
            // Even though vertex consumers do not need to be uploaded after 1.18.2,
            // we still need to call upload() since 1.12.2 requires it.
            builder.upload();
        });
    }
}
