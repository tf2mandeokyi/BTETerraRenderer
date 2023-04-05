package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface I18nConnector {
    I18nConnector INSTANCE = ImplFinder.search(I18nConnector.class);

    String format(String key, Object... parameters);
}
