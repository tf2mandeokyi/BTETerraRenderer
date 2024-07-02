package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UShort;
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
    private UByte versionMajor;
    private UByte versionMinor;
    private EncodedGeometryType encoderType;
    private MeshEncoderMethod encoderMethod;
    private UShort flags;
}
