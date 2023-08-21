package com.mndk.bteterrarenderer.core.util.mixin;

public class MixinUtil {
    /**
     * Throws {@link UnsupportedOperationException}
     * @param ignoredTempParameters Temporary parameters for avoiding unused warning
     */
    public static <T> T notOverwritten(Object... ignoredTempParameters) {
        throw new UnsupportedOperationException("mixin hasn't overwrite this method");
    }
}
