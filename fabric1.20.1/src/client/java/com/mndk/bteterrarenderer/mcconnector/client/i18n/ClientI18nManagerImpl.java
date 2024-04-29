package com.mndk.bteterrarenderer.mcconnector.client.i18n;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Language;

public class ClientI18nManagerImpl implements I18nManager {
    public String getCurrentLanguage() {
        return MinecraftClient.getInstance().getLanguageManager().getLanguage();
    }
    public String format(String key, Object... parameters) {
        return Language.getInstance().get(key);
    }
}
