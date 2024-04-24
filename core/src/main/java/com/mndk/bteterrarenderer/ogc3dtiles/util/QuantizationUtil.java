package com.mndk.bteterrarenderer.ogc3dtiles.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QuantizationUtil {

    /**
     * Converts {@code short(-32768 ~ 32767)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned short(0 ~ 65535)} to {@code float(0.0 ~ 1.0)}
     */
    public float normalizeShort(int quantized, boolean unsigned) {
        return unsigned ? quantized / 65535f : Math.max(quantized / 32767f, -1.0f);
    }

    /**
     * Converts {@code short(-32768 ~ 32767)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned short(0 ~ 65535)} to {@code float(0.0 ~ 1.0)}
     */
    public float normalizeShort(short quantized, boolean unsigned) {
        return unsigned ? Short.toUnsignedInt(quantized) / 65535f : Math.max(quantized / 32767f, -1.0f);
    }

    /**
     * Converts {@code short(-32768 ~ 32767)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned short(0 ~ 65535)} to {@code float(-1.0 ~ 1.0)}
     */
    public float sNormalizeShort(short quantized, boolean unsigned) {
        return unsigned ? Short.toUnsignedInt(quantized) / 65535f * 2 - 1 : Math.max(quantized / 32767f, -1.0f);
    }

    /**
     * Converts {@code short(-32768 ~ 32767)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned short(0 ~ 65535)} to {@code float(0.0 ~ 1.0)}
     */
    public float[] normalizeShorts(Short[] quantized, boolean unsigned) {
        float[] result = new float[quantized.length];
        for(int i = 0; i < quantized.length; i++) result[i] = normalizeShort(quantized[i], unsigned);
        return result;
    }

    /**
     * Converts {@code short(-32768 ~ 32767)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned short(0 ~ 65535)} to {@code float(-1.0 ~ 1.0)}
     */
    public float[] sNormalizeShorts(Short[] quantized, boolean unsigned) {
        float[] result = new float[quantized.length];
        for(int i = 0; i < quantized.length; i++) result[i] = sNormalizeShort(quantized[i], unsigned);
        return result;
    }

    /**
     * Converts {@code byte(-128 ~ 127)} to {@code float(-1.0 ~ 1.0)}<br>
     * or {@code unsigned byte(0 ~ 255)} to {@code float(0.0 ~ 1.0)}
     */
    public float normalizeByte(int quantized, boolean unsigned) {
        return unsigned ? quantized / 255f : Math.max(quantized / 127f, -1.0f);
    }

}
