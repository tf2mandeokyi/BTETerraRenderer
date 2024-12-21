package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileDataFormat;
import de.javagl.jgltf.model.GltfModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tileset extends TileData {

    private final Map<String, Object> asset;
    private final Map<String, TileProperty> properties;
    private final double geometricError;
    private final Tile rootTile;

    @JsonCreator
    public Tileset(
            @JsonProperty(value = "asset") Map<String, Object> asset,
            @Nullable @JsonProperty(value = "properties") Map<String, TileProperty> properties,
            @JsonProperty(value = "geometricError") double geometricError,
            @JsonProperty(value = "root") Tile rootTile
    ) {
        super(TileDataFormat.TILESET_JSON);
        this.asset = asset;
        this.properties = properties != null ? properties : new HashMap<>();
        this.geometricError = geometricError;
        this.rootTile = rootTile;
    }

    @Nullable
    @Override
    public GltfModel getGltfModelInstance() {
        return null;
    }
}
