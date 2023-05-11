package com.mndk.bteterrarenderer.connector.graphics;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class VertexFormatImpl {
    static VertexFormat toMinecraftVertexFormat(VertexFormatConnectorEnum vf) {
        switch(vf) {
            case POSITION: return DefaultVertexFormats.POSITION;
            case POSITION_TEX: return DefaultVertexFormats.POSITION_TEX;
            case POSITION_TEX_COLOR: return DefaultVertexFormats.POSITION_TEX_COLOR;
        }
        throw new RuntimeException("Invalid vertex format enum");
    }
}
