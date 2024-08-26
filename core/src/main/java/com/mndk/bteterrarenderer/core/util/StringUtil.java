package com.mndk.bteterrarenderer.core.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtil {
    public String formatDoubleNicely(double value, int maximumDecimalDigit) {
        return value == (long) value ?
                String.format("%d", (long) value) :
                String.format(String.format("%%.%df", maximumDecimalDigit), value);
    }

    public static int indexOf(Pattern pattern, String string, int fromIndex) {
        string = string.substring(fromIndex);
        Matcher matcher = pattern.matcher(string);
        return matcher.find() ? matcher.start() + fromIndex : -1;
    }
}
