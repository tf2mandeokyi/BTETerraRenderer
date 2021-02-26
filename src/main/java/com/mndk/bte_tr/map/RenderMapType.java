package com.mndk.bte_tr.map;

import com.mndk.bte_tr.util.TranslatableEnum;

import net.minecraft.client.resources.I18n;

public enum RenderMapType implements TranslatableEnum<RenderMapType> {
    PLAIN_MAP, AERIAL;

    @Override
    public String getTranslatedString() {
        return I18n.format( "enum.bte_tr.maptype." + super.toString());
    }
}
