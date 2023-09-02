package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tile {

    @ToString.Exclude
    private transient Tileset tilesetParent;
    @Nullable
    @ToString.Exclude
    private transient Tile tileParent;

    private final Volume boundingVolume;
    @Nullable
    private final Volume viewerRequestVolume;
    private final double geometricError;
    @Nullable
    private final TileRefinement refinement;
    @Nullable
    private final Matrix4 tileLocalTransform;
    private final List<TileContentLink> contents;
    private final List<Tile> children;

    @JsonCreator
    public Tile(@JsonProperty(value = "boundingVolume", required = true) Volume boundingVolume,
                @Nullable @JsonProperty(value = "viewerRequestVolume") Volume viewerRequestVolume,
                @JsonProperty(value = "geometricError", required = true) double geometricError,
                @Nullable @JsonProperty(value = "refine") TileRefinement refinement,
                @Nullable @JsonProperty(value = "transform") Matrix4 tileLocalTransform,
                @Nullable @JsonProperty(value = "contents") List<TileContentLink> contents,
                @Nullable @JsonProperty(value = "content") TileContentLink content,
                @Nullable @JsonProperty(value = "children") List<Tile> children) {

        this.boundingVolume = boundingVolume;
        this.viewerRequestVolume = viewerRequestVolume;
        this.geometricError = geometricError;
        this.refinement = refinement;
        this.tileLocalTransform = tileLocalTransform;
        this.children = children != null ? children : Collections.emptyList();
        this.contents = contents != null ? contents :
                (content != null ? Collections.singletonList(content) : Collections.emptyList());
    }

    /**
     * This must be called after the object construction.<br>
     * Normally this is called while the Tileset construction, so there's no need to worry about it
     * @param tilesetParent object's parent
     * @param tileParent object's direct parent. {@code null} if doesn't exist
     */
    void init(Tileset tilesetParent, Tile tileParent) {
        this.tilesetParent = tilesetParent;
        this.tileParent = tileParent;
        for(Tile child : children) {
            child.init(tilesetParent, this);
        }
        for(TileContentLink content : contents) {
            content.setTileParent(this);
        }
    }

    public Matrix4 getTilesetLocalTransform() {
        Matrix transform = this.tileLocalTransform;
        if(transform == null) transform = Matrix4.IDENTITY;

        Tile parent = this.tileParent;
        while(parent != null) {
            if(parent.getTileLocalTransform() != null) {
                transform = transform.multiply(parent.getTileLocalTransform());
            }
            parent = parent.getTileParent();
        }

        return transform.toMatrix4();
    }

    public Matrix4 getTrueTransform() {
        return this.getTilesetLocalTransform()
                .multiply(tilesetParent.getTrueTransform())
                .toMatrix4();
    }
}
