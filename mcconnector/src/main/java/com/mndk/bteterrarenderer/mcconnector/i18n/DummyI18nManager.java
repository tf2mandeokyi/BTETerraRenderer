package com.mndk.bteterrarenderer.mcconnector.i18n;

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
