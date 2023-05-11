package com.mndk.bteterrarenderer.connector.graphics;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public class VertexFormatImpl {
    static VertexFormat toMinecraftVertexFormat(VertexFormatConnectorEnum vf) {
        return switch (vf) {
            case POSITION -> DefaultVertexFormat.POSITION;
            case POSITION_TEX -> DefaultVertexFormat.POSITION_TEX;
            case POSITION_TEX_COLOR -> DefaultVertexFormat.POSITION_TEX_COLOR;
        };
    }
}
