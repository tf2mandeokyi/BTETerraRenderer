package com.mndk.mapdisp4bte.gui.option;

import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiBooleanOption extends GuiOption<Boolean> {

    public GuiBooleanOption(Supplier<Boolean> getter, Consumer<Boolean> setter, String name) {
        super(getter, setter, false, true, true, name);
    }

    @Override
    public Boolean getNext(Boolean current) {
        return true;
    }

    @Override
    public String getStringOf(Boolean value) {
        return value ? I18n.format("gui.yes") : I18n.format("gui.no");
    }
}
