package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum EncodedGeometryType {
    INVALID_GEOMETRY_TYPE(-1),
    POINT_CLOUD(0),
    TRIANGULAR_MESH(1);

    public static final int NUM_ENCODED_GEOMETRY_TYPES = (int) Stream.of(values())
            .filter(e -> e != INVALID_GEOMETRY_TYPE)
            .count();

    private final UByte value;

    EncodedGeometryType(int value) {
        this.value = UByte.of(value);
    }

    public static EncodedGeometryType fromValue(UByte value) {
        for(EncodedGeometryType type : values()) {
            if(type.value.equals(value)) return type;
        }
        return INVALID_GEOMETRY_TYPE;
    }
}
