package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** List of all mesh traversal methods supported by Draco framework. */
@Getter @RequiredArgsConstructor
public enum MeshTraversalMethod {
    MESH_TRAVERSAL_DEPTH_FIRST(0),
    MESH_TRAVERSAL_PREDICTION_DEGREE(1);

    public static final int NUM_TRAVERSAL_METHODS = values().length;

    private final int value;
}
