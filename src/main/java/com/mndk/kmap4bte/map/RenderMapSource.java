package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapSource implements IterableEnum<RenderMapSource> {
    KAKAO;

    private RenderMapSource next;

    public String toString() {
        return I18n.format("enum.kmap4bte.mapsource." + super.toString());
    }

    @Override
    public RenderMapSource next() {
        return next;
    }

    static {
        KAKAO.next = KAKAO;
    }
}
