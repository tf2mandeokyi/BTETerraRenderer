package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;

import javax.annotation.Nullable;

/** Different methods used for symbol entropy encoding. */
@Getter
public enum SymbolCodingMethod {
    SYMBOL_CODING_TAGGED(0),
    SYMBOL_CODING_RAW(1);

    public static final int NUM_SYMBOL_CODING_METHODS = values().length;

    private final UByte value;

    SymbolCodingMethod(int value) {
        this.value = UByte.of(value);
    }

    @Nullable
    public static SymbolCodingMethod valueOf(int value) {
        return valueOf(UByte.of(value));
    }

    @Nullable
    public static SymbolCodingMethod valueOf(UByte value) {
        for(SymbolCodingMethod method : values()) {
            if(method.value.equals(value)) return method;
        }
        return null;
    }
}
