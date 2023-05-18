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

    /**
     * Copied from 1.18.2's <code>net.minecraft.client.StringSplitter.getWordPosition()</code>
     */
    public static int getWordPosition(String text, int delta, int originalPosition, boolean p_92359_) {
        int i = originalPosition;
        boolean flag = delta < 0;
        int j = Math.abs(delta);

        for(int k = 0; k < j; ++k) {
            if (flag) {
                while(p_92359_ && i > 0 && (text.charAt(i - 1) == ' ' || text.charAt(i - 1) == '\n')) {
                    --i;
                }

                while(i > 0 && text.charAt(i - 1) != ' ' && text.charAt(i - 1) != '\n') {
                    --i;
                }
            } else {
                int l = text.length();
                int i1 = text.indexOf(32, i);
                int j1 = text.indexOf(10, i);
                if (i1 == -1 && j1 == -1) {
                    i = -1;
                } else if (i1 != -1 && j1 != -1) {
                    i = Math.min(i1, j1);
                } else if (i1 != -1) {
                    i = i1;
                } else {
                    i = j1;
                }

                if (i == -1) {
                    i = l;
                } else {
                    while(p_92359_ && i < l && (text.charAt(i) == ' ' || text.charAt(i) == '\n')) {
                        ++i;
                    }
                }
            }
        }

        return i;
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
