package com.mndk.bteterrarenderer.connector.minecraft.graphics;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GlFactor {
    CONSTANT_ALPHA(32771), CONSTANT_COLOR(32769),
    SRC_ALPHA(770), DST_ALPHA(772),
    SRC_COLOR(768), DST_COLOR(774),
    ONE(1), ZERO(0),
    ONE_MINUS_CONSTANT_ALPHA(32772), ONE_MINUS_CONSTANT_COLOR(32770),
    ONE_MINUS_SRC_ALPHA(771), ONE_MINUS_DST_ALPHA(773),
    ONE_MINUS_SRC_COLOR(769), ONE_MINUS_DST_COLOR(775),
    SRC_ALPHA_SATURATE(776, null);

    public final Integer srcFactor;
    public final Integer dstFactor;

    GlFactor(int factor) {
        this.srcFactor = this.dstFactor = factor;
    }
}
