package com.mndk.bteterrarenderer.util;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class BTRUtil {

    public int notNegative(int value, String name) {
        checkArgument(value >= 0, name + " must not be negative");
        return value;
    }

    public void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public boolean validateDouble(String s) {
        try { Double.parseDouble(s); }
        catch (NumberFormatException e) { return false; }
        return true;
    }

    public int clamp(int min, int value, int max) {
        return Math.max(min, Math.min(value, max));
    }

    public double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public boolean arrayStartsWith(byte[] array, @Nonnull byte[] start) {
        if (array == null) return false;
        if (array.length < start.length) return false;
        for (int i = 0; i < start.length; i++) {
            if (array[i] != start[i]) return false;
        }
        return true;
    }

    public <T extends Number> T doubleToNumber(Class<T> clazz, double value) {
        if (clazz == double.class || clazz == Double.class) return BTRUtil.uncheckedCast(value);
        else if (clazz == float.class || clazz == Float.class) return BTRUtil.uncheckedCast((float) value);
        else if (clazz == long.class || clazz == Long.class) return BTRUtil.uncheckedCast((long) value);
        else if (clazz == int.class || clazz == Integer.class) return BTRUtil.uncheckedCast((int) value);
        else if (clazz == short.class || clazz == Short.class) return BTRUtil.uncheckedCast((short) value);
        else if (clazz == byte.class || clazz == Byte.class) return BTRUtil.uncheckedCast((byte) value);
        else throw new RuntimeException("Not a number class: " + clazz);
    }

    public <T extends Number> T integerToNumber(Class<T> clazz, int value) {
        if (clazz == double.class || clazz == Double.class) return BTRUtil.uncheckedCast((double) value);
        else if (clazz == float.class || clazz == Float.class) return BTRUtil.uncheckedCast((float) value);
        else if (clazz == long.class || clazz == Long.class) return BTRUtil.uncheckedCast((long) value);
        else if (clazz == int.class || clazz == Integer.class) return BTRUtil.uncheckedCast(value);
        else if (clazz == short.class || clazz == Short.class) return BTRUtil.uncheckedCast((short) value);
        else if (clazz == byte.class || clazz == Byte.class) return BTRUtil.uncheckedCast((byte) value);
        else throw new RuntimeException("Not a number class: " + clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T uncheckedCast(Object object) {
        return (T) object;
    }
}
