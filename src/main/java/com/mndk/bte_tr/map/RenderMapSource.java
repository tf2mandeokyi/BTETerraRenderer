package com.mndk.bte_tr.map;

import com.mndk.bte_tr.map.bing.BingAerialMapManager;
import com.mndk.bte_tr.map.bing.BingPlainMapManager;
import com.mndk.bte_tr.map.kakao.KakaoAerialMapManager;
import com.mndk.bte_tr.map.kakao.KakaoPlainMapManager;
import com.mndk.bte_tr.map.naver.NaverMapManager;
import com.mndk.bte_tr.map.osm.OpenStreetMapManager;
import com.mndk.bte_tr.map.tmap.TMapManager;
import com.mndk.bte_tr.util.TranslatableEnum;

import net.minecraft.client.resources.I18n;

@Deprecated
public enum RenderMapSource implements TranslatableEnum<RenderMapSource> {
    OSM, BING_AERIAL, BING_PLAIN, KAKAO_AERIAL, KAKAO_PLAIN, TMAP, NAVER;

    private ExternalMapManager renderer;

    public String getTranslatedString() {
        return I18n.format("enum.bte_tr.mapsource." + super.toString());
    }

    public ExternalMapManager getMapRenderer() {
        return renderer;
    }

    static {
        KAKAO_AERIAL.renderer = new KakaoAerialMapManager();
        KAKAO_PLAIN.renderer = new KakaoPlainMapManager();
        OSM.renderer = new OpenStreetMapManager();
        BING_AERIAL.renderer = new BingAerialMapManager();
        BING_PLAIN.renderer = new BingPlainMapManager();
        TMAP.renderer = new TMapManager();
        NAVER.renderer = new NaverMapManager();
    }
}
