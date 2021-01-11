package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.map.kakao.KakaoMapRenderer;
import com.mndk.kmap4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements IterableEnum<RenderMapSource> {
    KAKAO("KAKAO");

    private RenderMapSource next;
    private final String enumName;
    private CustomMapRenderer renderer;

    RenderMapSource(String s) {
        this.enumName = s;
    }

    public String toString() {
        return I18n.format("enum.kmap4bte.mapsource." + super.toString());
    }

    @Override
    public RenderMapSource next() {
        return next;
    }

    @Override
    public String getEnumName() { return enumName; }

    public CustomMapRenderer getMapRenderer() {
        return renderer;
    }

    static {
        KAKAO.next = KAKAO;
        KAKAO.renderer = new KakaoMapRenderer();
    }
}
