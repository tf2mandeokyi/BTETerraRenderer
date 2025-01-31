package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import lombok.Data;
import org.joml.Vector2f;

import javax.annotation.Nullable;

@Data
class ParsedPoint {
    public final McCoord gamePos;
    public final Vector2f tex;
    @Nullable
    public final McCoord gameNormal;
}
