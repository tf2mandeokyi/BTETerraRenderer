package com.mndk.bteterrarenderer.mcconnector;

import java.util.Arrays;

public class MixinUtil {
    /**
     * Throws {@link UnsupportedOperationException}
     * @param ignoredTempParameters Temporary parameters to avoid unused warnings
     */
    public static <T> T notOverwritten(Object... ignoredTempParameters) {
        throw new UnsupportedOperationException("mixin hasn't overwrite this method (parameters: " +
                Arrays.toString(ignoredTempParameters) + ")");
    }
}
