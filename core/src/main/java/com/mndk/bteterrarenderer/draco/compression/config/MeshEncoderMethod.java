package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/** List of encoding methods for meshes. */
@Getter @RequiredArgsConstructor
public enum MeshEncoderMethod {
    MESH_SEQUENTIAL_ENCODING(0),
    MESH_EDGEBREAKER_ENCODING(1);

    private final int value;

    @Nullable
    public static MeshEncoderMethod valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static MeshEncoderMethod valueOf(int value) {
        for(MeshEncoderMethod method : values()) {
            if(method.value == value) return method;
        }
        return null;
    }
}
