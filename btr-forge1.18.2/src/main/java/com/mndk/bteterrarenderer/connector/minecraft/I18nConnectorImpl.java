package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.locale.Language;

@ConnectorImpl
@SuppressWarnings("unused")
public class I18nConnectorImpl implements I18nConnector {
    public String format(String key, Object... parameters) {
        return Language.getInstance().getOrDefault(key);
    }
}
