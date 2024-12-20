package com.mndk.bteterrarenderer.mcconnector.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MinecraftStringUtil {
    public static String filterMinecraftAllowedCharacters(String text) {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (isMinecraftAllowedCharacter(c)) {
                stringbuilder.append(c);
            }
        }

        return stringbuilder.toString();
    }

    public static boolean isMinecraftAllowedCharacter(char c) {
        return c != 0xa7 && c >= ' ' && c != 0x7f;
    }

    /**
     * Copied from 1.18.2's <code>net.minecraft.Util.offsetByCodepoints()</code>
     */
    public int offsetByCodepoints(String text, int originalPosition, int amount) {
        int i = text.length();
        if (amount >= 0) {
            for (int j = 0; originalPosition < i && j < amount; ++j) {
                if (Character.isHighSurrogate(text.charAt(originalPosition++)) && originalPosition < i && Character.isLowSurrogate(text.charAt(originalPosition))) {
                    ++originalPosition;
                }
            }
        } else {
            for (int k = amount; originalPosition > 0 && k < 0; ++k) {
                --originalPosition;
                if (Character.isLowSurrogate(text.charAt(originalPosition)) && originalPosition > 0 && Character.isHighSurrogate(text.charAt(originalPosition - 1))) {
                    --originalPosition;
                }
            }
        }
        return originalPosition;
    }
}
