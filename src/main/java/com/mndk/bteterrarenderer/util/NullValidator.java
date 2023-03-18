package com.mndk.bteterrarenderer.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NullValidator {

    public static <T> T get(@Nullable T value, @Nonnull T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
