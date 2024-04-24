package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTex extends GraphicsVertex<PosTex> {
    public final double x, y, z;
    public final float u, v;

    @Override
    public PosTex transformPosition(PositionTransformer transformer) {
        double[] result = transformer.transform(x, y, z);
        return new PosTex(result[0], result[1], result[2], u, v);
    }
}
