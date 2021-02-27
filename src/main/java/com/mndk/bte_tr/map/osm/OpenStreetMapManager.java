package com.mndk.bte_tr.map.osm;

import java.net.URLConnection;

import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.map.RenderMapSource;
import com.mndk.bte_tr.map.mercator.MercatorMapManager;

public class OpenStreetMapManager extends MercatorMapManager {

    public OpenStreetMapManager() {
        super(RenderMapSource.OSM, "https://{random}.tile.openstreetmap.org/{z}/{x}/{y}.png", 2);
    }

    private static int domain_num = 0;
    private static final char[] randomChars = {'a', 'b', 'c'};

    @Override
    protected String getRandom() {
        domain_num = domain_num >= 2 ? 0 : domain_num + 1;
        return randomChars[domain_num] + "";
    }

    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom) {

        URLConnection result = super.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, zoom);

        if(result == null) return null;

        result.setRequestProperty("User-Agent", BTETerraRenderer.MODID + "/" + BTETerraRenderer.VERSION + " Java/1.8");
        result.setUseCaches(true);

        return result;
    }
}
