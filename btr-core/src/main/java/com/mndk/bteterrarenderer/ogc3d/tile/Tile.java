package com.mndk.bteterrarenderer.ogc3d.tile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.mndk.bteterrarenderer.ogc3d.math.Matrix4;
import com.mndk.bteterrarenderer.ogc3d.math.volume.Volume;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@JsonDeserialize(builder = Tile.TileBuilder.class)
public class Tile {
    private final Volume boundingVolume;
    @Nullable
    private final Volume viewerRequestVolume;
    private final double geometricError;
    @Nullable
    private final TileRefinement refine;
    private final Matrix4 transform;
    private final List<TileContent> contents;
    private final List<Tile> children;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Accessors(fluent = true)
    static class TileBuilder {
        @Setter
        private TileContent content;

        @SuppressWarnings("unused")
        public Tile build() {
            List<TileContent> contents = this.contents != null ? this.contents :
                    (this.content != null ? Collections.singletonList(this.content) : Collections.emptyList());
            Matrix4 transform = this.transform != null ? this.transform : Matrix4.IDENTITY;

            return new Tile(boundingVolume, viewerRequestVolume, geometricError, refine, transform, contents, children);
        }
    }
}
