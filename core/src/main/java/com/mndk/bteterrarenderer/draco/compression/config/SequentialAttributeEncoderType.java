package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/**
 * List of various sequential attribute encoder/decoders that can be used in our
 * pipeline. The values represent unique identifiers used by the decoder and
 * they should not be changed.
 */
@Getter @RequiredArgsConstructor
public enum SequentialAttributeEncoderType {
    GENERIC(0),
    INTEGER(1),
    QUANTIZATION(2),
    NORMALS(3);

    private final int value;

    @Nullable
    public static SequentialAttributeEncoderType valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static SequentialAttributeEncoderType valueOf(int value) {
        for (SequentialAttributeEncoderType type : values()) {
            if (type.value == value) return type;
        }
        return null;
    }
}
