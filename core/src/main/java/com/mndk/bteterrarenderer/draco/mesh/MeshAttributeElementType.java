package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public enum MeshAttributeElementType {
    VERTEX(0),
    CORNER(1),
    FACE(2);

    public final int value;

    @Nullable
    public static MeshAttributeElementType valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static MeshAttributeElementType valueOf(int value) {
        for(MeshAttributeElementType type : values()) {
            if(type.value == value) return type;
        }
        return null;
    }
}
