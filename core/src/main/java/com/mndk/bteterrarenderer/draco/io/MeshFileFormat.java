package com.mndk.bteterrarenderer.draco.io;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public enum MeshFileFormat {
    OBJ("obj"), PLY("ply"), DRACO("drc");

    private final String extension;

    @Nullable
    public static MeshFileFormat fromExtension(String extension) {
        for(MeshFileFormat format : values()) {
            if(format.extension.equals(extension)) return format;
        }
        return null;
    }
}
