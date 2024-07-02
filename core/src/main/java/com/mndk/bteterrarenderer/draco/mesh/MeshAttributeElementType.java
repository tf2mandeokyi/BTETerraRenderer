package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public enum MeshAttributeElementType {
    MESH_VERTEX_ATTRIBUTE(0),
    MESH_CORNER_ATTRIBUTE(1),
    MESH_FACE_ATTRIBUTE(2);

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
