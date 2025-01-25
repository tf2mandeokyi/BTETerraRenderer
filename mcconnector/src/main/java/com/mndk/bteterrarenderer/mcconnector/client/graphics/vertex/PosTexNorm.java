package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTexNorm implements GraphicsVertex {
    public final McCoord pos;
    public final float u, v;
    public final McCoord normal;

    public PosTexNorm transform(McCoordTransformer transformer) {
        McCoord newPos = transformer.transform(pos);
        McCoord newNormal = transformer.transform(pos.add(normal)).subtract(newPos);
        return new PosTexNorm(newPos, u, v, newNormal);
    }

    @Override
    public String toString() {
        return String.format("PosTexNorm(pos=%s, tex=[%.4f, %.4f], norm=%s)", pos, u, v, normal);
    }
}
