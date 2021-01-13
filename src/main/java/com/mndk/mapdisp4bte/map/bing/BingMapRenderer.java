package com.mndk.mapdisp4bte.map.bing;

import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.map.mercator.MercatorMapRenderer;

public class BingMapRenderer extends MercatorMapRenderer {
    private static final String plainMapTemplate = "https://t.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{u}?it=G,LC,BX,RL&shading=hill";
    private static final String aerialTemplate = "https://t.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{u}?it=A&shading=hill";

    public BingMapRenderer() {
        super(RenderMapSource.BING, plainMapTemplate, aerialTemplate, 2);
    }

    @Override
    public String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type) {
        String template = type == RenderMapType.AERIAL ? aerialTemplate : plainMapTemplate;
        return template.replace("{u}", BingTileConverter.tileToQuadKey(tileX, tileY, 18 - level));
    }
}
