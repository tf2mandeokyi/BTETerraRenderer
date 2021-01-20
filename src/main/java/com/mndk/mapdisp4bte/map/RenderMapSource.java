package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.map.bing.BingMapManager;
import com.mndk.mapdisp4bte.map.kakao.KakaoMapManager;
import com.mndk.mapdisp4bte.map.naver.NaverMapManager;
import com.mndk.mapdisp4bte.map.osm.OpenStreetMapManager;
import com.mndk.mapdisp4bte.map.tmap.TMapManager;
import com.mndk.mapdisp4bte.util.TranslatableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements TranslatableEnum<RenderMapSource> {
    OSM, BING, KAKAO, TMAP, NAVER;

    private ExternalMapManager renderer;

    public String getTranslatedString() {
        return I18n.format("enum.mapdisp4bte.mapsource." + super.toString());
    }

    public ExternalMapManager getMapRenderer() {
        return renderer;
    }

    static {
        KAKAO.renderer = new KakaoMapManager();
        OSM.renderer = new OpenStreetMapManager();
        BING.renderer = new BingMapManager();
        TMAP.renderer = new TMapManager();
        NAVER.renderer = new NaverMapManager();
    }
}
