package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;

import java.io.InputStream;

@Data
public class PreParsedData {
    private final Matrix4 transform;
    private final InputStream stream;
}
