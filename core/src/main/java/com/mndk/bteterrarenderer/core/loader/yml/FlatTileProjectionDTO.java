package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import lombok.Data;
import lombok.Value;

import java.util.Map;

@Data
@Value
@JsonDeserialize
public class FlatTileProjectionDTO {

    Map<String, FlatTileProjectionImpl> tileProjections;

    @JsonCreator
    public FlatTileProjectionDTO(
            @JsonProperty(value = "tile_projections", required = true)
            Map<String, FlatTileProjectionImpl> tileProjections
    ) {
        this.tileProjections = tileProjections;
        this.tileProjections.forEach((key, value) -> value.setName(key));
    }
}
