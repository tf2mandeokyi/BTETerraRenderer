package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;

public interface DataIOManager<T> {
    // IO operations
    long size();
    T read(UByteArray array, long index, Endian endian);
    void write(UByteArray array, long index, T value, Endian endian);
}
