package com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosXY implements GraphicsVertex {
    public final float x, y;

    @Override
    public String toString() {
        return String.format("PosXY(pos=[%.2f, %.2f])", x, y);
    }
}
