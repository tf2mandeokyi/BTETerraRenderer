package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import lombok.Data;

import javax.annotation.Nullable;

@Data
class ParsedPoint {
    public final Cartesian3 gamePos;
    public final float[] tex;
    @Nullable
    public final Cartesian3 gameNormal;
}
