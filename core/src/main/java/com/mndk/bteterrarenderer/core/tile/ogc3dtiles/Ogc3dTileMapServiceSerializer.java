package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceSerializer;

import java.io.IOException;

class Ogc3dTileMapServiceSerializer extends TileMapServiceSerializer<Ogc3dTileMapService> {
    protected Ogc3dTileMapServiceSerializer() {
        super(Ogc3dTileMapService.class);
    }

    @Override
    protected void serializeTMS(Ogc3dTileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("semi_major", value.getCoordConverter().getSemiMajorAxis());
        gen.writeNumberField("semi_minor", value.getCoordConverter().getSemiMinorAxis());
        gen.writeBooleanField("rotate_model_x", value.isRotateModelAlongEarthXAxis());
        gen.writeStringField("geoid", value.getGeoidType());
    }
}
