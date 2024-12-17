package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosXY extends GraphicsVertex<PosXY> {
    public final float x, y;

    @Override
    public PosXY transformMcCoord(McCoordTransformer transformer) {
        // This method is meaningless for this class, but we'll implement it anyway
        McCoord result = transformer.transform(new McCoord(x, y, 0));
        return new PosXY((float) result.getX(), result.getY());
    }

    @Override
    public String toString() {
        return String.format("PosXY(pos=[%.2f, %.2f])", x, y);
    }
}
