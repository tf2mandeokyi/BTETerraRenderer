package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.util.TranslatableEnum;
import net.minecraft.client.resources.I18n;

public enum RenderMapType implements TranslatableEnum<RenderMapType> {
    PLAIN_MAP, AERIAL;

    @Override
    public String getTranslatedString() {
        return I18n.format( "enum.mapdisp4bte.maptype." + super.toString());
    }
}
