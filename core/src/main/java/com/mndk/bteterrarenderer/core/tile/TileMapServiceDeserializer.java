package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public abstract class TileMapServiceDeserializer<T extends AbstractTileMapService<?>> extends JsonDeserializer<T> {
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = ctxt.readTree(p);
        TileMapServiceCommonProperties properties = ctxt.readTreeAsValue(node, TileMapServiceCommonProperties.class);
        return this.deserialize(node, properties, ctxt);
    }

    protected abstract T deserialize(JsonNode node, TileMapServiceCommonProperties properties, DeserializationContext ctxt)
            throws IOException;
}
