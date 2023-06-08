package com.mndk.bteterrarenderer.connector.minecraft;

import net.minecraft.locale.Language;

public class I18nConnectorImpl18 implements I18nConnector {
    public String format(String key, Object... parameters) {
        return Language.getInstance().getOrDefault(key);
    }
}
