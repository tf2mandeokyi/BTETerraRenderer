package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.loader.LoaderRegistry;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceCommonProperties;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceDeserializer;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;

import java.io.IOException;

class FlatTileMapServiceDeserializer extends TileMapServiceDeserializer<FlatTileMapService> {
    @Override
    protected FlatTileMapService deserialize(JsonNode node, TileMapServiceCommonProperties properties, DeserializationContext ctxt) throws IOException {
        int defaultZoom = JsonParserUtil.getOrDefault(node, "default_zoom", FlatTileMapService.DEFAULT_ZOOM);
        boolean invertZoom = JsonParserUtil.getOrDefault(node, "invert_zoom", false);
        boolean invertLatitude = JsonParserUtil.getOrDefault(node, "invert_lat", false);
        boolean flipVertically = JsonParserUtil.getOrDefault(node, "flip_vert", false);

        // Get projection
        FlatTileProjection projection = LoaderRegistry.flatProj().get(node.get("projection"));

        // Modify projection
        FlatTileCoordTranslator coordTranslator = new FlatTileCoordTranslator(projection)
                .setDefaultZoom(defaultZoom)
                .setInvertZoom(invertZoom)
                .setInvertLatitude(invertLatitude)
                .setFlipVertically(flipVertically);

        return FlatTileMapService.builder()
                .properties(properties)
                .coordTranslator(coordTranslator)
                .urlConverter(new FlatTileURLConverter(defaultZoom, invertZoom))
                .build();
    }
}
