package com.mndk.bteterrarenderer.core.loader.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.util.category.CategoryMap;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

@Getter
@Value
@JsonDeserialize
public class TileMapServicePropertyDTO {

    CategoryMap<Map<String, Object>> categories;

    @JsonCreator
    public TileMapServicePropertyDTO(
            @JsonProperty(value = "categories", required = true)
            CategoryMap<Map<String, Object>> categories
    ) {
        this.categories = categories;
    }
}
