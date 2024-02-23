package com.mndk.bteterrarenderer.mcconnector.client.i18n;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;

public class ClientI18nManagerImpl implements I18nManager {
    public String getCurrentLanguage() {
        return Minecraft.getInstance().getLanguageManager().getSelected().getCode();
    }
    public String format(String key, Object... parameters) {
        return Language.getInstance().getOrDefault(key);
    }
}
