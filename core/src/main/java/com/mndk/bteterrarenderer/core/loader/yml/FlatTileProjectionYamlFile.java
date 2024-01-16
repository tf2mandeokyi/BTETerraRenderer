package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import lombok.Data;

import java.util.Map;

@Data
@JsonDeserialize
public class FlatTileProjectionYamlFile {
    public final Map<String, FlatTileProjectionImpl> tileProjections;

    @JsonCreator
    public FlatTileProjectionYamlFile(@JsonProperty(value = "tile_projections", required = true)
                                      Map<String, FlatTileProjectionImpl> tileProjections) {
        this.tileProjections = tileProjections;
    }
}
