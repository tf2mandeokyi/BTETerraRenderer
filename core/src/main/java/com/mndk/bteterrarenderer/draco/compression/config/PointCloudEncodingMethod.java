package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** List of encoding methods for point clouds. */
@Getter @RequiredArgsConstructor
public enum PointCloudEncodingMethod {
    POINT_CLOUD_SEQUENTIAL_ENCODING(0),
    POINT_CLOUD_KD_TREE_ENCODING(1);

    private final int value;
}
