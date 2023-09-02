package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Getter
@ToString
public class TileContentLink {

    @Nullable
    @ToString.Exclude
    @Setter(AccessLevel.PACKAGE)
    private transient Tile tileParent;

    private final String uri;
    @Nullable
    private final Volume boundingVolume;
    @Nullable
    private final Integer group;

    public TileContentLink(@JsonProperty(value = "uri", required = true) String uri,
                           @Nullable @JsonProperty(value = "boundingVolume") Volume boundingVolume,
                           @Nullable @JsonProperty(value = "group") Integer group) {
        this.uri = uri;
        this.boundingVolume = boundingVolume;
        this.group = group;
    }

    /**
     * Preserves the parent's url query part
     * @return The url
     */
    public URL getUrl() throws MalformedURLException {
        if (tileParent == null) return new URL(uri);

        URL parentUrl = tileParent.getTilesetParent().getUrl();
        if (parentUrl == null) return new URL(uri);
        String parentQueryPart = parentUrl.getQuery();

        URL result = new URL(parentUrl, uri);
        String resultQueryPart = result.getQuery();
        String resultString = result.toString();

        if(parentQueryPart != null) {
            String delimiter = resultQueryPart != null ? "&" : "?";
            resultString += delimiter + parentQueryPart;
        }
        return new URL(resultString);
    }

    public TileData fetch() throws IOException {
        if (tileParent == null) throw new NullPointerException("parent is null");
        return TileResourceManager.fetch(
                this.getUrl(),
                tileParent.getTilesetParent(),
                tileParent.getTilesetLocalTransform()
        );
    }
}
