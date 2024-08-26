package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/** List of encoding methods for meshes. */
@Getter @RequiredArgsConstructor
public enum MeshEncoderMethod {
    SEQUENTIAL(0),
    EDGEBREAKER(1);

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
