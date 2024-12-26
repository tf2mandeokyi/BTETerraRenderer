package com.mndk.bteterrarenderer.mcconnector.client.graphics.shape;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.AbstractBufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.GraphicsVertex;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordAABB;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.BTRUtil;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicsShapes {
    private final Map<DrawingFormat<?, ?>, List<GraphicsShape<?>>> shapeMap = new HashMap<>();
    @Getter @Nullable private McCoordAABB boundingBox = null;

    public <T extends GraphicsVertex<T>, U extends GraphicsShape<T>> void add(DrawingFormat<T, U> format, U shape) {
        List<U> list = BTRUtil.uncheckedCast(shapeMap.computeIfAbsent(format, key -> new ArrayList<>()));
        list.add(shape);
        McCoordAABB shapeBoundingBox = shape.getBoundingBox();
        boundingBox = boundingBox == null ? shapeBoundingBox : boundingBox.include(shapeBoundingBox);
    }

    public void drawAndRender(DrawContextWrapper drawContextWrapper,
                              McCoordTransformer modelPosTransformer, float alpha) {
        shapeMap.forEach((format, shapes) -> {
            format.setShader(McConnector.client().glGraphicsManager);
            BufferBuilderWrapper builder = format.begin(drawContextWrapper);
            for (GraphicsShape<?> shape : shapes) {
                format.nextShape(drawContextWrapper, builder, BTRUtil.uncheckedCast(shape), modelPosTransformer, alpha);
            }
            builder.drawAndRender();
        });
    }
}
