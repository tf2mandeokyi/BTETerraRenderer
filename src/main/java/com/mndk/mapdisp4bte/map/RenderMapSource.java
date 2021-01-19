package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.map.bing.BingMapRenderer;
import com.mndk.mapdisp4bte.map.kakao.KakaoMapRenderer;
import com.mndk.mapdisp4bte.map.naver.NaverMapRenderer;
import com.mndk.mapdisp4bte.map.osm.OpenStreetMapRenderer;
import com.mndk.mapdisp4bte.map.tmap.TMapRenderer;
import com.mndk.mapdisp4bte.util.TranslatableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements TranslatableEnum<RenderMapSource> {
    OSM, BING, KAKAO, TMAP, NAVER;

    private ExternalMapRenderer renderer;

    public String getTranslatedString() {
        return I18n.format("enum.mapdisp4bte.mapsource." + super.toString());
    }

    public ExternalMapRenderer getMapRenderer() {
        return renderer;
    }

    static {
        KAKAO.renderer = new KakaoMapRenderer();
        OSM.renderer = new OpenStreetMapRenderer();
        BING.renderer = new BingMapRenderer();
        TMAP.renderer = new TMapRenderer();
        NAVER.renderer = new NaverMapRenderer();
    }
}
