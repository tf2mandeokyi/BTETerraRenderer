package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** Draco header V1 */
@Getter
@Setter
@RequiredArgsConstructor
public class DracoHeader {
    /** Mask for setting and getting the bit for metadata in {@link #flags} of header. */
    public static final int METADATA_FLAG_MASK = 0x8000;

    private String dracoString;
    private short versionMajor;
    private short versionMinor;
    private short encoderType;
    private short encoderMethod;
    private int flags;
}
