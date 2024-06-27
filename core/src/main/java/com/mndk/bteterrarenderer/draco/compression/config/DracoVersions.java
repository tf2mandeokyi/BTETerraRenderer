package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.number.UByte;

public class DracoVersions {

    // Latest Draco bit-stream version.
    /** {@code kDracoPointCloudBitstreamVersionMajor} */
    public static final byte POINT_CLOUD_BIT_STREAM_VERSION_MAJOR = 2;
    /** {@code kDracoPointCloudBitstreamVersionMinor} */
    public static final byte POINT_CLOUD_BIT_STREAM_VERSION_MINOR = 3;
    /** {@code kDracoMeshBitstreamVersionMajor} */
    public static final byte MESH_BIT_STREAM_VERSION_MAJOR = 2;
    /** {@code kDracoMeshBitstreamVersionMinor} */
    public static final byte MESH_BIT_STREAM_VERSION_MINOR = 2;

    // Concatenated latest bit-stream version.
    /** {@code kDracoPointCloudBitstreamVersion} */
    public static final int POINT_CLOUD_BIT_STREAM_VERSION = getBitstreamVersion(
            POINT_CLOUD_BIT_STREAM_VERSION_MAJOR, POINT_CLOUD_BIT_STREAM_VERSION_MINOR);
    /** {@code kDracoMeshBitstreamVersion} */
    public static final int MESH_BIT_STREAM_VERSION = getBitstreamVersion(
            MESH_BIT_STREAM_VERSION_MAJOR, MESH_BIT_STREAM_VERSION_MINOR);

    public static int getBitstreamVersion(UByte major, UByte minor) {
        return getBitstreamVersion(major.intValue(), minor.intValue());
    }
    public static int getBitstreamVersion(int major, int minor) {
        return (short) ((major << 8) | minor);
    }
}
