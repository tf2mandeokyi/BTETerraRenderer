package com.mndk.bteterrarenderer.draco.mesh;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeshAttributeElementType {
    MESH_VERTEX_ATTRIBUTE(0),
    MESH_CORNER_ATTRIBUTE(1),
    MESH_FACE_ATTRIBUTE(2);

    public final int index;
}
