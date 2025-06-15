package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;

import java.io.IOException;

public abstract class TileMapServiceSerializer<T extends AbstractTileMapService<?>> extends JsonSerializer<T> {
    private final String type;

    protected TileMapServiceSerializer(Class<T> clazz) {
        this.type = TileMapServiceParseRegistries.TYPE_MAP.inverse().get(clazz);
    }

    @Override
    public final void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", this.type);

        TileMapServiceCommonProperties.from(value).write(gen);
        this.serializeTMS(value, gen, serializers);
        gen.writeEndObject();
    }

    protected abstract void serializeTMS(T value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException;
}
