package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Different methods used for symbol entropy encoding. */
@Getter @RequiredArgsConstructor
public enum SymbolCodingMethod {
    SYMBOL_CODING_TAGGED(0),
    SYMBOL_CODING_RAW(1);

    public static final int NUM_SYMBOL_CODING_METHODS = values().length;

    private final int value;
}
