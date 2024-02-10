package com.mndk.bteterrarenderer.core.gui.mcfx.button;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;

public class McFXBooleanButton extends McFXButton {

    private final PropertyAccessor<Boolean> value;
    private final String prefix;

    public McFXBooleanButton(PropertyAccessor<Boolean> value, String prefix) {
        super(prefix + booleanToFormattedI18n(value.get()), (self, mouseButton) -> {
            value.set(!value.get());
            self.setDisplayString(prefix + booleanToFormattedI18n(value.get()));
        });
        this.value = value;
        this.prefix = prefix;
    }

    @Override
    protected void init() {
        super.init();
        this.setDisplayString(prefix + booleanToFormattedI18n(value.get()));
    }

    private static String booleanToFormattedI18n(boolean b) {
        return b ?
                "§a" + I18nManager.format("options.on") :
                "§c" + I18nManager.format("options.off");
    }

}
