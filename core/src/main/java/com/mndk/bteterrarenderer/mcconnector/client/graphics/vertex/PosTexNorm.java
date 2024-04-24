package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTexNorm extends GraphicsVertex<PosTexNorm> {
    public final double px, py, pz;
    public final float u, v;
    public final double nx, ny, nz;

    @Override
    public PosTexNorm transformPosition(PositionTransformer transformer) {
        double[] posResult = transformer.transform(px, py, pz);
        double[] normResult = transformer.transform(px + nx, py + ny, pz + nz);
        return new PosTexNorm(
                posResult[0], posResult[1], posResult[2],
                u, v,
                normResult[0] - posResult[0], normResult[1] - posResult[1], normResult[2] - posResult[2]
        );
    }
}
