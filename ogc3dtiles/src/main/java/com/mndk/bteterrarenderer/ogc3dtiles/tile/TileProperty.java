package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = TileProperty.TilePropertyBuilder.class)
public class TileProperty {
    private final double minimum, maximum;

    public String toString() {
        return "[" + minimum + " ~ " + maximum + "]";
    }

    @JsonPOJOBuilder(withPrefix = "")
    static class TilePropertyBuilder {}
}
