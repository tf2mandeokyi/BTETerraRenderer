package com.mndk.bteterrarenderer.util;

public class StringUtil {
    public static String formatDoubleNicely(double value, int maximumDecimalDigit) {
        return value == (long) value ?
                String.format("%d", (long) value) :
                String.format(String.format("%%.%df", maximumDecimalDigit), value);
    }

    /**
     * Copied from 1.18.2's <code>net.minecraft.Util.offsetByCodepoints()</code>
     */
    public static int offsetByCodepoints(String text, int originalPosition, int amount) {
        int i = text.length();
        if (amount >= 0) {
            for(int j = 0; originalPosition < i && j < amount; ++j) {
                if (Character.isHighSurrogate(text.charAt(originalPosition++)) && originalPosition < i && Character.isLowSurrogate(text.charAt(originalPosition))) {
                    ++originalPosition;
                }
            }
        } else {
            for(int k = amount; originalPosition > 0 && k < 0; ++k) {
                --originalPosition;
                if (Character.isLowSurrogate(text.charAt(originalPosition)) && originalPosition > 0 && Character.isHighSurrogate(text.charAt(originalPosition - 1))) {
                    --originalPosition;
                }
            }
        }
        return originalPosition;
    }

    public static String filterMinecraftAllowedCharacters(String text) {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (isMinecraftAllowedCharacter(c)) {
                stringbuilder.append(c);
            }
        }

        return stringbuilder.toString();
    }

    public static boolean isMinecraftAllowedCharacter(char p_136189_) {
        return p_136189_ != 167 && p_136189_ >= ' ' && p_136189_ != 127;
    }
}
