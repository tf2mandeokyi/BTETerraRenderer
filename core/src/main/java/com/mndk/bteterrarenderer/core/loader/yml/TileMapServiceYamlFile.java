package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import lombok.Getter;

@Getter
@JsonDeserialize
public class TileMapServiceYamlFile {

    private final CategoryMap<TileMapService<?>> categories;

    @JsonCreator
    public TileMapServiceYamlFile(@JsonProperty(value = "categories", required = true)
                                  CategoryMap<TileMapService<?>> categories) {
        this.categories = categories;
    }
}
