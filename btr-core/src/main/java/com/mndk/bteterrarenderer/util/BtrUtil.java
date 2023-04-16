package com.mndk.bteterrarenderer.util;

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

    public static <T> T validateNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static boolean validateDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String formatDoubleNicely(double value, int maximumDecimalDigit) {
        return value == (long) value ?
                String.format("%d", (long) value) :
                String.format(String.format("%%%d.f", maximumDecimalDigit), value);
    }

    public static String formatDoubleNicely(double value) {
        return value == (long) value ? String.format("%d", (long) value) : String.format("%f", value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object object) {
        return (T) object;
    }
}
