package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/** List of all mesh traversal methods supported by Draco framework. */
@Getter @RequiredArgsConstructor
public enum MeshTraversalMethod {
    DEPTH_FIRST(0),
    PREDICTION_DEGREE(1);

    public static final int NUM_TRAVERSAL_METHODS = values().length;

    private final int value;

    @Nullable
    public static MeshTraversalMethod valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static MeshTraversalMethod valueOf(int value) {
        for(MeshTraversalMethod method : values()) {
            if(method.value == value) return method;
        }
        return null;
    }
}
