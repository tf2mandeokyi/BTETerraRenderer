package com.mndk.bteterrarenderer.core.util;

import javax.annotation.Nonnull;

public class BtrUtil {

    public static int notNegative(int value, String name) {
        checkArgument(value >= 0, name + " must not be negative");
        return value;
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static boolean validateDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    public static boolean arrayStartsWith(byte[] array, @Nonnull byte[] start) {
        if(array == null) return false;
        if(array.length < start.length) return false;
        for(int i = 0; i < start.length; i++) {
            if(array[i] != start[i]) return false;
        }
        return true;
    }

    public static <T extends Number> T doubleToNumber(Class<T> clazz, double value) {
        if(clazz == double.class || clazz == Double.class) return BtrUtil.uncheckedCast(value);
        else if(clazz == float.class || clazz == Float.class) return BtrUtil.uncheckedCast((float) value);
        else if(clazz == long.class || clazz == Long.class) return BtrUtil.uncheckedCast((long) value);
        else if(clazz == int.class || clazz == Integer.class) return BtrUtil.uncheckedCast((int) value);
        else if(clazz == short.class || clazz == Short.class) return BtrUtil.uncheckedCast((short) value);
        else if(clazz == byte.class || clazz == Byte.class) return BtrUtil.uncheckedCast((byte) value);
        else throw new RuntimeException("Not a number class: " + clazz);
    }

    public static <T extends Number> T integerToNumber(Class<T> clazz, int value) {
        if(clazz == double.class || clazz == Double.class) return BtrUtil.uncheckedCast((double) value);
        else if(clazz == float.class || clazz == Float.class) return BtrUtil.uncheckedCast((float) value);
        else if(clazz == long.class || clazz == Long.class) return BtrUtil.uncheckedCast((long) value);
        else if(clazz == int.class || clazz == Integer.class) return BtrUtil.uncheckedCast(value);
        else if(clazz == short.class || clazz == Short.class) return BtrUtil.uncheckedCast((short) value);
        else if(clazz == byte.class || clazz == Byte.class) return BtrUtil.uncheckedCast((byte) value);
        else throw new RuntimeException("Not a number class: " + clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object object) {
        return (T) object;
    }
}
