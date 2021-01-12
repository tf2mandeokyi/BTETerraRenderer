package com.mndk.kmap4bte.map.osm;

import com.mndk.kmap4bte.ModReference;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.map.mercator.MercatorMapRenderer;

import java.net.URLConnection;

public class OpenStreetMapRenderer extends MercatorMapRenderer {

    public OpenStreetMapRenderer() {
        super(RenderMapSource.OSM, "https://tile.openstreetmap.org/{z}/{x}/{y}.png");
    }

    private static int domain_num = 0;
    private static final char[] asdf = {'a', 'b', 'c'};

    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {

        URLConnection result = super.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, level, type);

        if(result == null) return null;
        /*domain_num++;
        if(domain_num >= 3) domain_num = 0;*/

        result.setRequestProperty("User-Agent", ModReference.MODID + "/" + ModReference.VERSION + " Java/1.8");
        result.setUseCaches(true);

        return result;
    }
}
