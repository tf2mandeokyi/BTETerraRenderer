package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public enum EdgeFaceName {
    LEFT(0), RIGHT(1);

    private final int value;

    @Nullable
    public static EdgeFaceName valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static EdgeFaceName valueOf(int value) {
        for(EdgeFaceName face : values()) {
            if(face.value == value) return face;
        }
        return null;
    }
}
