package com.mndk.bteterrarenderer.mcconnector.client.i18n;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class ClientI18nManagerImpl implements I18nManager {
    public String getCurrentLanguage() {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
    }
    public String format(String key, Object... parameters) {
        return I18n.format(key);
    }
}
