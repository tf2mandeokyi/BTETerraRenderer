package com.mndk.mapdisp4bte.util;

public class StringToNumber {

    public static boolean validate(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String formatNicely(double value) {
        return value == (long) value ? String.format("%d", (long) value) : String.format("%s", value);
    }
}
