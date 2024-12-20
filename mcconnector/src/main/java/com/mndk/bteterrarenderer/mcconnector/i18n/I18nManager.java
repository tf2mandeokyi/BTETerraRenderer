package com.mndk.bteterrarenderer.mcconnector.i18n;

public interface I18nManager {
    String getCurrentLanguage();
    String format(String key, Object... parameters);
}
