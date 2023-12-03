package com.mndk.bteterrarenderer.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public String formatDoubleNicely(double value, int maximumDecimalDigit) {
        return value == (long) value ?
                String.format("%d", (long) value) :
                String.format(String.format("%%.%df", maximumDecimalDigit), value);
    }
}
