package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface I18nConnector {
    I18nConnector INSTANCE = ImplFinder.search();

    String format(String key, Object... parameters);
}
