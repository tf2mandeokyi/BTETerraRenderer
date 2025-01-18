package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.math.JOMLUtils;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import lombok.Getter;
import lombok.ToString;
import org.joml.Matrix4d;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tile {

    private final Volume boundingVolume;
    @Nullable
    private final Volume viewerRequestVolume;
    private final double geometricError;
    @Nullable
    private final TileRefinement refinement;
    @Nullable @JsonIgnore
    private final Matrix4d tileLocalTransform;
    private final List<TileContentLink> contents;
    private final List<Tile> children;

    @JsonCreator
    public Tile(
            @JsonProperty(value = "boundingVolume", required = true) Volume boundingVolume,
            @Nullable @JsonProperty(value = "viewerRequestVolume") Volume viewerRequestVolume,
            @JsonProperty(value = "geometricError", required = true) double geometricError,
            @Nullable @JsonProperty(value = "refine") TileRefinement refinement,
            @Nullable @JsonProperty(value = "transform") double[] tileLocalTransform,
            @Nullable @JsonProperty(value = "contents") List<TileContentLink> contents,
            @Nullable @JsonProperty(value = "content") TileContentLink content,
            @Nullable @JsonProperty(value = "children") List<Tile> children
    ) {
        this.boundingVolume = boundingVolume;
        this.viewerRequestVolume = viewerRequestVolume;
        this.geometricError = geometricError;
        this.refinement = refinement;
        this.tileLocalTransform = tileLocalTransform != null ? JOMLUtils.columnMajor4d(tileLocalTransform) : null;
        this.children = children != null ? children : Collections.emptyList();
        this.contents = contents != null ? contents :
                (content != null ? Collections.singletonList(content) : Collections.emptyList());
    }
}
