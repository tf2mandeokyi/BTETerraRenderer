package com.mndk.bteterrarenderer.ogc3dtiles.tile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import com.mndk.bteterrarenderer.ogc3dtiles.util.URLUtil;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TileContentLink {

    private final String uri;
    @Nullable private final Volume boundingVolume;
    @Nullable private final Integer group;

    public TileContentLink(
            @JsonProperty(value = "uri", required = true) String uri,
            @Nullable @JsonProperty(value = "boundingVolume") Volume boundingVolume,
            @Nullable @JsonProperty(value = "group") Integer group
    ) {
        this.uri = uri;
        this.boundingVolume = boundingVolume;
        this.group = group;
    }

    /**
     * Preserves the parent's url query part
     * @return The url
     */
    public URL getTrueUrl(URL parentUrl) throws MalformedURLException {
        return URLUtil.combineUri(parentUrl, this.uri);
    }
}
