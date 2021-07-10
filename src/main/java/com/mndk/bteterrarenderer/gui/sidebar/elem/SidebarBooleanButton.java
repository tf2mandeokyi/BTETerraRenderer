package com.mndk.bteterrarenderer.gui.sidebar.elem;

import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.resources.I18n;

public class SidebarBooleanButton extends SidebarButton {

    public SidebarBooleanButton(GetterSetter<Boolean> value, String prefix) {
        super(prefix + booleanToI18n(value.get()), (self, mouseButton) -> {
            value.set(!value.get());
            self.setDisplayString(prefix + booleanToI18n(value.get()));
        });
    }

    private static String booleanToI18n(boolean b) {
        return b ?
                I18n.format("gui.bteterrarenderer.maprenderer.enabled") :
                I18n.format("gui.bteterrarenderer.maprenderer.disabled");
    }

}
