package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicsShapes {
    private final Map<DrawingFormat<?, ?>, List<GraphicsShape<?>>> shapeMap = new HashMap<>();

    public <T extends GraphicsVertex<T>, U extends GraphicsShape<T>> void add(DrawingFormat<T, U> format, U shape) {
        List<U> list = BTRUtil.uncheckedCast(shapeMap.computeIfAbsent(format, key -> new ArrayList<>()));
        list.add(shape);
    }

    public <T extends GraphicsVertex<T>, U extends GraphicsShape<T>> List<U> getShapesForFormat(DrawingFormat<T, U> format) {
        return BTRUtil.uncheckedCast(shapeMap.get(format));
    }

    public void drawAndRender(DrawContextWrapper<?> drawContextWrapper,
                              PositionTransformer transformer, float alpha) {
        shapeMap.forEach((format, shapes) -> {
            BufferBuilderWrapper<?> builder = drawContextWrapper.tessellatorBufferBuilder();

            format.setShader(McConnector.client().glGraphicsManager);
            format.begin(builder);
            for(GraphicsShape<?> shape : shapes) {
                format.nextShape(drawContextWrapper, BTRUtil.uncheckedCast(shape), transformer, alpha);
            }
            builder.drawAndRender();
        });
    }
}
