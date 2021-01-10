package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.util.IterableEnum;

public enum RenderMapType implements IterableEnum<RenderMapType> {
    PLAIN_MAP("Plain map"), AERIAL("Aerial");

    private RenderMapType next;
    private final String name;

    RenderMapType(String name) { this.name = name; }

    @Override
    public RenderMapType next() {
        return next;
    }

    @Override
    public String toString() {
        return name;
    }

    static {
        PLAIN_MAP.next = AERIAL;
        AERIAL.next = PLAIN_MAP;
    }
}
