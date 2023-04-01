package com.mndk.bteterrarenderer.util;

public class NullValidator {

    public static <T> T get(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
