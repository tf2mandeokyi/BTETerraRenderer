package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.joml.Vector2f;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTexNorm implements GraphicsVertex {
    public final McCoord pos;
    public final Vector2f tex;
    public final McCoord normal;

    public PosTexNorm transform(McCoordTransformer transformer) {
        McCoord newPos = transformer.transform(pos);
        McCoord newNormal = transformer.transform(pos.add(normal)).subtract(newPos);
        return new PosTexNorm(newPos, tex, newNormal);
    }

    @Override
    public String toString() {
        return String.format("PosTexNorm(pos=%s, tex=%s, norm=%s)", pos, tex, normal);
    }
}
