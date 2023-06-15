package com.mndk.bteterrarenderer.ogc3d.tile;

import com.mndk.bteterrarenderer.ogc3d.math.volume.Volume;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;

@Data
@Builder
@Jacksonized
public class TileContent {
    private final String uri;
    @Nullable
    private final Volume boundingVolume;
    @Nullable
    private final Integer group;

    public String getAbsoluteUri(String tilesetSource) {
        int index = tilesetSource.lastIndexOf('/') + 1;
        if(index == 0) index = tilesetSource.length();
        String parentUri = tilesetSource.substring(0, index);
        return parentUri + this.uri;
    }
}
