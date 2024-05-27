package com.mndk.bteterrarenderer.draco_deprecated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum DracoGeometryType {
    POINT_CLOUD,
    TRIANGULAR_MESH;

    private static final Map<Byte, DracoGeometryType> MAP = new HashMap<Byte, DracoGeometryType>() {{
        put((byte) 0, DracoGeometryType.POINT_CLOUD);
        put((byte) 1, DracoGeometryType.TRIANGULAR_MESH);
    }};

    public static DracoGeometryType valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
