package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.core.util.i18n.Translatable;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;

public class DummyI18nManager implements I18nManager {
    @Override
    public String getCurrentLanguage() {
        return Translatable.DEFAULT_KEY;
    }

    @Override
    public String format(String key, Object... parameters) {
        return key;
    }
}
