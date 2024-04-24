package com.mndk.bteterrarenderer.core.loader.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonDeserialize
public class TileMapServicePropertyJsonFile {

    private final CategoryMap<Map<String, Object>> categories;

    @JsonCreator
    public TileMapServicePropertyJsonFile(
            @JsonProperty(value = "categories", required = true)
            CategoryMap<Map<String, Object>> categories
    ) {
        this.categories = categories;
    }
}
