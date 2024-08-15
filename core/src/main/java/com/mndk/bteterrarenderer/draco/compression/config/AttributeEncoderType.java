package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * List of various attribute encoders supported by our framework. The entries
 * are used as unique identifiers of the encoders and their values should not
 * be changed!
 */
@Getter @RequiredArgsConstructor
public enum AttributeEncoderType {
    BASIC_ATTRIBUTE_ENCODER(0),
    MESH_TRAVERSAL_ATTRIBUTE_ENCODER(1),
    KD_TREE_ATTRIBUTE_ENCODER(2);

    private final int value;
}
