package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.resources.I18n;

@ConnectorImpl
@SuppressWarnings("unused")
public class I18nConnectorImpl implements I18nConnector {
    public String format(String key, Object... parameters) {
        return I18n.format(key);
    }
}
