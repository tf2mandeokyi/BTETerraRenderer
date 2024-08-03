package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/**
 * List of all variant of the edgebreaker method that is used for compression
 * of mesh connectivity.
 */
@Getter @RequiredArgsConstructor
public enum MeshEdgebreakerConnectivityEncodingMethod {
    STANDARD(0),
    PREDICTIVE(1),  // Deprecated.
    VALENCE(2);

    private final int value;

    @Nullable
    public static MeshEdgebreakerConnectivityEncodingMethod valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static MeshEdgebreakerConnectivityEncodingMethod valueOf(int value) {
        for(MeshEdgebreakerConnectivityEncodingMethod method : values()) {
            if(method.value == value) return method;
        }
        return null;
    }
}
