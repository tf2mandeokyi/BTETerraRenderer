package com.mndk.bteterrarenderer.connector.graphics;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class VertexFormatImpl {
    static VertexFormat toMinecraftVertexFormat(VertexFormatConnectorEnum vf) {
        switch(vf) {
            case BLOCK: return DefaultVertexFormats.BLOCK;
            case ITEM: return DefaultVertexFormats.ITEM;
            case OLDMODEL_POSITION_TEX_NORMAL: return DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL;
            case PARTICLE_POSITION_TEX_COLOR_LMAP: return DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
            case POSITION: return DefaultVertexFormats.POSITION;
            case POSITION_COLOR: return DefaultVertexFormats.POSITION_COLOR;
            case POSITION_TEX: return DefaultVertexFormats.POSITION_TEX;
            case POSITION_NORMAL: return DefaultVertexFormats.POSITION_NORMAL;
            case POSITION_TEX_COLOR: return DefaultVertexFormats.POSITION_TEX_COLOR;
            case POSITION_TEX_NORMAL: return DefaultVertexFormats.POSITION_TEX_NORMAL;
            case POSITION_TEX_LMAP_COLOR: return DefaultVertexFormats.POSITION_TEX_LMAP_COLOR;
            case POSITION_TEX_COLOR_NORMAL: return DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
        }
        throw new RuntimeException("Invalid vertex format enum");
    }
}
