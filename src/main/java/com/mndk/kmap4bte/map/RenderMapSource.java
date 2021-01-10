package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.util.IterableEnum;

public enum RenderMapSource implements IterableEnum<RenderMapSource> {
    KAKAO("Kakao Map");

    private final String name;
    private RenderMapSource next;
    RenderMapSource(String name) { this.name = name; }

    public String toString() {
        return name;
    }

    @Override
    public RenderMapSource next() {
        return next;
    }

    static {
        KAKAO.next = KAKAO;
    }
}
