package com.mndk.bteterrarenderer.mcconnector.util.math;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
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

    public McCoord multiply(double scalar) {
        return new McCoord(x * scalar, (float) (y * scalar), z * scalar);
    }

    public McCoord cross(McCoord other) {
        return new McCoord(
                y * other.z - z * other.y,
                (float) (z * other.x - x * other.z),
                x * other.y - y * other.x
        );
    }

    public McCoord normalized() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new McCoord(x / length, (float) (y / length), z / length);
    }

    @Override
    public String toString() {
        return String.format("[%.2f, %.2f, %.2f]", x, y, z);
    }

    public static McCoord fromYawPitch(double yawDegrees, double pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double x = -Math.cos(pitch) * Math.sin(yaw);
        float y = (float) -Math.sin(pitch);
        double z = Math.cos(pitch) * Math.cos(yaw);
        return new McCoord(x, y, z);
    }

    public static McCoord min(McCoord a, McCoord b) {
        return new McCoord(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
    }
    public static McCoord max(McCoord a, McCoord b) {
        return new McCoord(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }
}
