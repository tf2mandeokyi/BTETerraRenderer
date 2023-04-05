package com.mndk.bteterrarenderer.gui.sidebar.button;

import com.mndk.bteterrarenderer.connector.minecraft.I18nConnector;
import com.mndk.bteterrarenderer.util.GetterSetter;

public class SidebarBooleanButton extends SidebarButton {

    private final GetterSetter<Boolean> value;
    private final String prefix;

    public SidebarBooleanButton(GetterSetter<Boolean> value, String prefix) {
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
                "§a" + I18nConnector.INSTANCE.format("options.on") :
                "§c" + I18nConnector.INSTANCE.format("options.off");
    }

}
