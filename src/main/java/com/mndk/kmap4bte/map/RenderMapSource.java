package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.map.bing.BingMapRenderer;
import com.mndk.kmap4bte.map.kakao.KakaoMapRenderer;
import com.mndk.kmap4bte.map.osm.OpenStreetMapRenderer;
import com.mndk.kmap4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements IterableEnum<RenderMapSource> {
    KAKAO, OSM, BING;

    private RenderMapSource next;
    private ExternalMapRenderer renderer;

    public String toString() {
        return I18n.format("enum.kmap4bte.mapsource." + super.toString());
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

        BING.next = KAKAO;
        BING.renderer = new BingMapRenderer();
    }
}
