package com.mndk.bteterrarenderer.mcconnector.dummy;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;

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
