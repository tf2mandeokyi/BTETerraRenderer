package com.mndk.bteterrarenderer.ogc3d.tile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonDeserialize(builder = Tileset.TilesetBuilder.class)
public class Tileset {
    private final Map<String, Object> asset;
    private final Map<String, TileProperty> properties;
    private final double geometricError;
    private final Tile root;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TilesetBuilder {}
}
