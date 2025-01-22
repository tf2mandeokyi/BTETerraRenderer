package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTexNorm implements GraphicsVertex {
    public final McCoord pos;
    public final float u, v;
    public final McCoord normal;

    @Override
    public String toString() {
        return String.format("PosTexNorm(pos=%s, tex=[%.4f, %.4f], norm=%s)", pos, u, v, normal);
    }
}
