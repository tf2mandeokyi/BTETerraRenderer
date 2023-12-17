package com.mndk.bteterrarenderer.mcconnector.i18n;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class I18nManager {
    public String getCurrentLanguage() {
        return MixinUtil.notOverwritten();
    }
    public String format(String key, Object... parameters) {
        return MixinUtil.notOverwritten(key, parameters);
    }
}
