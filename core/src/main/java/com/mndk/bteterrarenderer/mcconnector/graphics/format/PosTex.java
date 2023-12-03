package com.mndk.bteterrarenderer.mcconnector.graphics.format;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PosTex extends VertexInfo {
    public final double x, y, z;
    public final float u, v;
}
