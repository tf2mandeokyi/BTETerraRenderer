package com.mndk.bteterrarenderer.core.graphics.format;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosXY extends VertexInfo {
    public final float x, y;
}
