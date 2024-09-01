package com.mndk.bteterrarenderer.mcconnector.util.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class McCoord {
    private final double x;
    // We don't need a double floating-point precision for y coordinates,
    // since it's not common to have a y value that is too large.
    private final float y;
    private final double z;

    public McCoord add(McCoord other) {
        return new McCoord(x + other.x, y + other.y, z + other.z);
    }

    public McCoord subtract(McCoord other) {
        return new McCoord(x - other.x, y - other.y, z - other.z);
    }

    public McCoord cross(McCoord other) {
        return new McCoord(
                y * other.z - z * other.y,
                (float) (z * other.x - x * other.z),
                x * other.y - y * other.x
        );
    }

    @Override
    public String toString() {
        return String.format("[%.2f, %.2f, %.2f]", x, y, z);
    }
}
