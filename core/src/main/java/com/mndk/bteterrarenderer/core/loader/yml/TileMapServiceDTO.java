package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.util.category.CategoryMap;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@JsonDeserialize
public class TileMapServiceDTO {

    CategoryMap<TileMapService> categories;

    @JsonCreator
    public TileMapServiceDTO(
            @JsonProperty(value = "categories", required = true)
            CategoryMap<TileMapService> categories
    ) {
        this.categories = categories;
    }
}
