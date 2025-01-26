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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tile {

    @Nullable @JsonIgnore
    private Tile parent;

    // For memoization
    @Nullable @JsonIgnore
    private Matrix4d tilesetLocalTransform;

    @Getter
    private final Volume boundingVolume;

    @Getter @Nullable
    private final Volume viewerRequestVolume;

    /**
     * From 6.7.1.2. Geometric error:
     * <p>
     * Tiles are structured into a tree incorporating Hierarchical Level of Detail (HLOD)
     * so that at runtime a client implementation will need to determine if a tile is
     * sufficiently detailed for rendering and if the content of tiles should be successively
     * refined by children tiles of higher resolution. An implementation will consider a
     * maximum allowed Screen-Space Error (SSE), the error measured in pixels.
     * <p>
     * A tile’s geometric error defines the selection metric for that tile. Its value is a
     * nonnegative number that specifies the error, in meters, of the tile’s simplified
     * representation of its source geometry. Generally, the root tile will have the largest
     * geometric error, and each successive level of children will have a smaller geometric
     * error than its parent, with leaf tiles having a geometric error of or close to 0.
     * <p>
     * In a client implementation, geometric error is used with other screen space metrics —
     * e.g., distance from the tile to the camera, screen size, and resolution — to calculate
     * the SSE introduced if this tile is rendered and its children are not. If the introduced
     * SSE exceeds the maximum allowed, then the tile is refined and its children are
     * considered for rendering.
     * <p>
     * The geometric error is formulated based on a metric like point density, mesh or
     * texture decimation, or another factor specific to that tileset. In general, a higher
     * geometric error means a tile will be refined more aggressively, and children tiles
     * will be loaded and rendered sooner.
     */
    @Getter
    private final double geometricError;

    /**
     * From 6.7.1.3. Refinement:
     * <p>
     * Refinement determines the process by which a lower resolution parent tile renders
     * when its higher resolution children are selected to be rendered. Permitted refinement
     * types are replacement ("REPLACE") and additive ("ADD"). If the tile has replacement
     * refinement, the children tiles are rendered in place of the parent, that is, the
     * parent tile is no longer rendered. If the tile has additive refinement, the children
     * are rendered in addition to the parent tile.
     * <p>
     * A tileset can use replacement refinement exclusively, additive refinement exclusively,
     * or any combination of additive and replacement refinement.
     * <p>
     * A refinement type is required for the root tile of a tileset; it is optional for all
     * other tiles. When omitted, a tile inherits the refinement type of its parent.
     */
    @Nullable
    private TileRefinement refinement;

    @Nullable @JsonIgnore
    private final Matrix4d tileLocalTransform;

    @Getter
    private final List<TileContentLink> contents;

    @Getter
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

        for (Tile child : this.children) child.parent = this;
    }

    @Nonnull
    private Matrix4d getTilesetLocalTransform() {
        if (this.tilesetLocalTransform != null) return this.tilesetLocalTransform;
        Matrix4d parentTransform = this.parent != null ? new Matrix4d(this.parent.getTilesetLocalTransform()) : new Matrix4d();
        Matrix4d result = this.tileLocalTransform != null ? this.tileLocalTransform : new Matrix4d();
        this.tilesetLocalTransform = parentTransform.mul(result);
        return this.tilesetLocalTransform;
    }

    @Nonnull
    public Matrix4d getGlobalTransform(Matrix4d parentTilesetTransform) {
        Matrix4d result = new Matrix4d(parentTilesetTransform);
        Matrix4d tilesetLocalTransform = this.getTilesetLocalTransform();
        result.mul(tilesetLocalTransform);
        return result;
    }

    @Nonnull
    public TileRefinement getRefinement() {
        if (this.refinement != null) return this.refinement;
        if (this.parent == null) throw new IllegalStateException("No parent tile");
        this.refinement = this.parent.getRefinement();
        return this.refinement;
    }
}
