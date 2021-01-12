package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.util.IterableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapType implements IterableEnum<RenderMapType> {
    PLAIN_MAP("PLAIN_MAP"), AERIAL("AERIAL");

    private RenderMapType next;
    private final String enumName;

    RenderMapType(String s) { this.enumName = s; }

    @Override
    public RenderMapType next() {
        return next;
    }

    @Override
    public String getEnumName() { return enumName; }

    @Override
    public String toString() {
        return I18n.format( "enum.mapdisp4bte.maptype." + super.toString());
    }

    static {
        PLAIN_MAP.next = AERIAL;
        AERIAL.next = PLAIN_MAP;
    }
}
