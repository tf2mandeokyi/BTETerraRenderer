package com.mndk.bteterrarenderer.tms.url;

import com.mndk.bteterrarenderer.tms.TileMapService;
import lombok.Setter;

public abstract class TileURLConverter {

    @Setter
    protected int defaultZoom = TileMapService.DEFAULT_ZOOM;
    @Setter
    protected boolean invertZoom = false;

    public final String convertToUrl(String template, int tileX, int tileY, int relativeZoom) {
        return this.convert(template, tileX, tileY, defaultZoom + (invertZoom ? -relativeZoom : relativeZoom));
    }

    protected abstract String convert(String template, int tileX, int tileY, int absoluteZoom);

}
