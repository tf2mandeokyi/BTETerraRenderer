package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceSerializer;

import java.io.IOException;

class FlatTileMapServiceSerializer extends TileMapServiceSerializer<FlatTileMapService> {
    protected FlatTileMapServiceSerializer() {
        super(FlatTileMapService.class);
    }

    @Override
    public void serializeTMS(FlatTileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        FlatTileCoordTranslator translator = value.getCoordTranslator();
        gen.writeNumberField("default_zoom", translator.getDefaultZoom());
        gen.writeBooleanField("invert_zoom", translator.isInvertZoom());
        gen.writeBooleanField("invert_lat", translator.isInvertLatitude());
        gen.writeBooleanField("flip_vert", translator.isFlipVertically());

        FlatTileProjection projection = translator.getProjection();
        if (projection.getName() != null) {
            gen.writeStringField("projection", projection.getName());
        } else {
            gen.writeObjectField("projection", projection);
        }
    }
}
