package com.mndk.mapdisp4bte.map.osm;

import com.mndk.mapdisp4bte.ModReference;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.map.mercator.MercatorMapRenderer;

import java.net.URLConnection;

public class OpenStreetMapRenderer extends MercatorMapRenderer {

    public OpenStreetMapRenderer() {
        super(RenderMapSource.OSM,
                "https://{random}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "https://{random}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                2);
    }

    private static int domain_num = 0;
    private static final char[] asdf = {'a', 'b', 'c'};

    @Override
    protected String getRandom() {
        domain_num = domain_num >= 2 ? 0 : domain_num + 1;
        return asdf[domain_num] + "";
    }

    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom, RenderMapType type) {

        URLConnection result = super.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, zoom, type);

        if(result == null) return null;

        result.setRequestProperty("User-Agent", ModReference.MODID + "/" + ModReference.VERSION + " Java/1.8");
        result.setUseCaches(true);

        return result;
    }
}
