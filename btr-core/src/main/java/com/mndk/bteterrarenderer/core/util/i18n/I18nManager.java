package com.mndk.bteterrarenderer.core.util.i18n;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class I18nManager {
    public String format(String key, Object... parameters) {
        return MixinUtil.notOverwritten(key, parameters);
    }
}
