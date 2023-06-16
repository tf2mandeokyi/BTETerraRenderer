package com.mndk.bteterrarenderer.ogc3d.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.function.Function;

@RequiredArgsConstructor
/*
 * TODO: change these to GL constants or something
 */
public enum BinaryComponentType {
    BYTE(byte.class, 1, 0, ByteBuffer::get),
    UNSIGNED_BYTE(byte.class, 1, 1, ByteBuffer::get),
    SHORT(short.class, 2, 0, ByteBuffer::getShort),
    UNSIGNED_SHORT(short.class, 2, 1, ByteBuffer::getShort),
    INT(int.class, 4, 0, ByteBuffer::getInt),
    UNSIGNED_INT(int.class, 4, 1, ByteBuffer::getInt),
    FLOAT(float.class, 4, -1, ByteBuffer::getFloat),
    DOUBLE(double.class, 8, -1, ByteBuffer::getDouble);

    private final Class<?> clazz;
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
        for(BinaryComponentType type : values()) {
            if(clazz != type.clazz) continue;
            if(type.unsignedState == -1) return type;
            if(unsigned ^ type.unsignedState == 1) return type;
        }
        return null;
    }
}
