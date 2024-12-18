package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTex extends GraphicsVertex<PosTex> {
    public final McCoord pos;
    public final float u, v;

    @Override
    public PosTex transformMcCoord(McCoordTransformer transformer) {
        McCoord result = transformer.transform(pos);
        return new PosTex(result, u, v);
    }

    @Override
    public McCoord getMcCoord() {
        return pos;
    }

    @Override
    public String toString() {
        return String.format("PosTex(pos=%s, tex=[%.4f, %.4f])", pos, u, v);
    }
}
