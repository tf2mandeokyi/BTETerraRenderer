package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.map.bing.BingMapRenderer;
import com.mndk.mapdisp4bte.map.kakao.KakaoMapRenderer;
import com.mndk.mapdisp4bte.map.osm.OpenStreetMapRenderer;
import com.mndk.mapdisp4bte.map.tmap.TMapRenderer;
import com.mndk.mapdisp4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements IterableEnum<RenderMapSource> {
    KAKAO, OSM, BING, TMAP;

    private RenderMapSource next;
    private ExternalMapRenderer renderer;

    public String toString() {
        return I18n.format("enum.mapdisp4bte.mapsource." + super.toString());
    }

    @Override
    public RenderMapSource next() {
        return next;
    }

    @Override
    public String getEnumName() { return super.toString(); }

    public ExternalMapRenderer getMapRenderer() {
        return renderer;
    }

    static {
        KAKAO.next = OSM;
        KAKAO.renderer = new KakaoMapRenderer();

        OSM.next = BING;
        OSM.renderer = new OpenStreetMapRenderer();

        BING.next = TMAP;
        BING.renderer = new BingMapRenderer();

        TMAP.next = KAKAO;
        TMAP.renderer = new TMapRenderer();
    }
}
