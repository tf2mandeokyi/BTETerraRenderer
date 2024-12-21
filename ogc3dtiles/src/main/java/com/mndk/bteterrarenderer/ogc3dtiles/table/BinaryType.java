package com.mndk.bteterrarenderer.ogc3dtiles.table;

import com.mndk.bteterrarenderer.util.BTRUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@Getter
@RequiredArgsConstructor
/*
 * TODO: Separating "VecN"s shouldn't be a good idea. Try Number[] instead
 */
public enum BinaryType {
    SCALAR(1, null, null),
    VEC2(2, BinaryVector.Vec2.class, BinaryVector.Vec2::new),
    VEC3(3, BinaryVector.Vec3.class, BinaryVector.Vec3::new),
    VEC4(4, BinaryVector.Vec4.class, BinaryVector.Vec4::new);

    private final int componentCount;
    private final Class<?> vectorClass;
    private final VectorGeneratorFunction<?> generator;

    public boolean isVector() {
        return vectorClass != null;
    }

    public Object readBinary(ByteBuffer buffer, BinaryComponentType type) {
        if (!this.isVector()) {
            return type.readBinary(buffer);
        }

        Object[] resultArray = new Object[this.componentCount];
        for (int i = 0; i < this.componentCount; i++) {
            resultArray[i] = type.readBinary(buffer);
        }

        return generator.apply(BTRUtil.uncheckedCast(resultArray));
    }

    public int getBinarySize(BinaryComponentType type) {
        return componentCount * type.getBinarySize();
    }

    public static BinaryType valueOf(Class<?> clazz) {
        for (BinaryType type : values()) {
            if (type == null) continue;
            if (clazz == type.vectorClass) return type;
        }
        return SCALAR;
    }

    public interface VectorGeneratorFunction<T> {
        BinaryVector<T> apply(T[] array);
    }
}
