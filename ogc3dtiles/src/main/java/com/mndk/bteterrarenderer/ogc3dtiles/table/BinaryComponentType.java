package com.mndk.bteterrarenderer.ogc3dtiles.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
/*
 * TODO: change these to GL constants or something
 */
public enum BinaryComponentType {
    BYTE(Arrays.asList(Byte.class, byte.class), 1, 0, ByteBuffer::get),
    UNSIGNED_BYTE(Arrays.asList(Byte.class, byte.class), 1, 1, ByteBuffer::get),
    SHORT(Arrays.asList(Short.class, short.class), 2, 0, ByteBuffer::getShort),
    UNSIGNED_SHORT(Arrays.asList(Short.class, short.class), 2, 1, ByteBuffer::getShort),
    INT(Arrays.asList(Integer.class, int.class), 4, 0, ByteBuffer::getInt),
    UNSIGNED_INT(Arrays.asList(Integer.class, int.class), 4, 1, ByteBuffer::getInt),
    FLOAT(Arrays.asList(Float.class, float.class), 4, -1, ByteBuffer::getFloat),
    DOUBLE(Arrays.asList(Double.class, double.class), 8, -1, ByteBuffer::getDouble);

    private final List<Class<?>> clazz;
    @Getter
    private final int binarySize;
    private final int unsignedState;
    private final Function<ByteBuffer, Object> readFunction;

    public Object readBinary(ByteBuffer buffer) {
        return readFunction.apply(buffer);
    }

    public boolean isUnsigned() {
        return unsignedState == 1;
    }

    public static BinaryComponentType valueOf(Class<?> clazz, boolean unsigned) {
        for (BinaryComponentType type : values()) {
            if (!type.clazz.contains(clazz)) continue;
            if (type.unsignedState == -1) return type;
            if (unsigned && type.isUnsigned()) return type;
            if (!unsigned && !type.isUnsigned()) return type;
        }
        return null;
    }
}
