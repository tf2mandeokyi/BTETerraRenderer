package com.mndk.bteterrarenderer.core.util;

import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class ArrayUtil {

    public <T> T[] expandOne(T[] target, T object, Function<Integer, T[]> arrayNew) {
        T[] result = arrayNew.apply(target.length + 1);
        System.arraycopy(target, 0, result, 0, target.length);
        result[target.length] = object;
        return result;
    }

    public int[] expandOne(int[] target, int object) {
        int[] result = new int[target.length + 1];
        System.arraycopy(target, 0, result, 0, target.length);
        result[target.length] = object;
        return result;
    }

}
