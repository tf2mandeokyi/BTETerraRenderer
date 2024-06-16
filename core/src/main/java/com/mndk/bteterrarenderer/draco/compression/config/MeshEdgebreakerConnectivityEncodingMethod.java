package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * List of all variant of the edgebreaker method that is used for compression
 * of mesh connectivity.
 */
@Getter @RequiredArgsConstructor
public enum MeshEdgebreakerConnectivityEncodingMethod {
    MESH_EDGEBREAKER_STANDARD_ENCODING(0),
    MESH_EDGEBREAKER_PREDICTIVE_ENCODING(1),  // Deprecated.
    MESH_EDGEBREAKER_VALENCE_ENCODING(2);

    private final int value;
}
