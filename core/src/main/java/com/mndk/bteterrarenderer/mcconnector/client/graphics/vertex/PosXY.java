package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosXY extends GraphicsVertex<PosXY> {
    public final float x, y;

    @Override
    public PosXY transformPosition(PositionTransformer transformer) {
        double[] result = transformer.transform(x, y, 0);
        return new PosXY((float) result[0], (float) result[1]);
    }
}
