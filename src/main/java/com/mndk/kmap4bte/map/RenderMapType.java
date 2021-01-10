package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapType implements IterableEnum<RenderMapType> {
    PLAIN_MAP, AERIAL;

    private RenderMapType next;

    @Override
    public RenderMapType next() {
        return next;
    }

    @Override
    public String toString() {
        return I18n.format( "enum.kmap4bte.maptype." + super.toString());
    }

    static {
        PLAIN_MAP.next = AERIAL;
        AERIAL.next = PLAIN_MAP;
    }
}
