package com.mndk.bteterrarenderer.core.ogc3d.table;

import com.mndk.bteterrarenderer.core.ogc3d.table.BinaryVector.Vec2;
import com.mndk.bteterrarenderer.core.ogc3d.table.BinaryVector.Vec3;
import com.mndk.bteterrarenderer.core.ogc3d.table.BinaryVector.Vec4;
import com.mndk.bteterrarenderer.core.util.BtrUtil;
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
    VEC2(2, Vec2.class, Vec2::new),
    VEC3(3, Vec3.class, Vec3::new),
    VEC4(4, Vec4.class, Vec4::new);

    private final int componentCount;
    private final Class<?> vectorClass;
    private final VectorGeneratorFunction<?> generator;

    public boolean isVector() {
        return vectorClass != null;
    }

    public Object readBinary(ByteBuffer buffer, BinaryComponentType type) {
        if(!this.isVector()) {
            return type.readBinary(buffer);
        }

        Object[] resultArray = new Object[this.componentCount];
        for(int i = 0; i < this.componentCount; i++) {
            resultArray[i] = type.readBinary(buffer);
        }

        return generator.apply(BtrUtil.uncheckedCast(resultArray));
    }

    public int getBinarySize(BinaryComponentType type) {
        return componentCount * type.getBinarySize();
    }

    public static BinaryType valueOf(Class<?> clazz) {
        for(BinaryType type : values()) {
            if(type == null) continue;
            if(clazz == type.vectorClass) return type;
        }
        return SCALAR;
    }

    public interface VectorGeneratorFunction<T> {
        BinaryVector<T> apply(T[] array);
    }
}
