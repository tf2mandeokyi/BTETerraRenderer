package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTexNorm extends GraphicsVertex<PosTexNorm> {
    public final McCoord pos;
    public final float u, v;
    public final McCoord normal;

    @Override
    public PosTexNorm transformMcCoord(McCoordTransformer transformer) {
        McCoord posResult = transformer.transform(pos);
        McCoord normResult = transformer.transform(pos.add(normal));
        return new PosTexNorm(posResult, u, v, normResult.subtract(posResult));
    }

    @Override
    public McCoord getMcCoord() {
        return pos;
    }

    @Override
    public String toString() {
        return String.format("PosTexNorm(pos=%s, tex=[%.4f, %.4f], norm=%s)", pos, u, v, normal);
    }
}
