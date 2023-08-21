package com.mndk.bteterrarenderer.core.ogc3d.tile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.mndk.bteterrarenderer.core.ogc3d.OgcFileContent;
import com.mndk.bteterrarenderer.core.ogc3d.OgcFileType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = Tileset.TilesetBuilder.class)
public class Tileset implements OgcFileContent {
    private final Map<String, Object> asset;
    private final Map<String, TileProperty> properties;
    private final double geometricError;
    private final Tile root;

    @Override
    public OgcFileType getFileType() { return OgcFileType.TILESET_JSON; }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TilesetBuilder {}
}
