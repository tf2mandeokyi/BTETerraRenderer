package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTex implements GraphicsVertex {
    public final McCoord pos;
    public final float u, v;

    @Override
    public String toString() {
        return String.format("PosTex(pos=%s, tex=[%.4f, %.4f])", pos, u, v);
    }
}
