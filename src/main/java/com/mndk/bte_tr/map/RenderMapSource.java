package com.mndk.bte_tr.map;

import com.mndk.bte_tr.map.bing.BingMapManager;
import com.mndk.bte_tr.map.kakao.KakaoMapManager;
import com.mndk.bte_tr.map.naver.NaverMapManager;
import com.mndk.bte_tr.map.osm.OpenStreetMapManager;
import com.mndk.bte_tr.map.tmap.TMapManager;
import com.mndk.bte_tr.util.TranslatableEnum;

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
