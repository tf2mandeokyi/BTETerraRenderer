package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.joml.Vector2f;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTex implements GraphicsVertex {
    public final McCoord pos;
    public final Vector2f tex;

    public PosTex(McCoord pos, float texU, float texV) {
        this(pos, new Vector2f(texU, texV));
    }

    @Override
    public String toString() {
        return String.format("PosTex(pos=%s, tex=%s)", pos, tex);
    }
}
