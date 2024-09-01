package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.Data;

import javax.annotation.Nullable;

@Data
class ParsedPoint {
    public final McCoord gamePos;
    public final float[] tex;
    @Nullable
    public final McCoord gameNormal;
}
