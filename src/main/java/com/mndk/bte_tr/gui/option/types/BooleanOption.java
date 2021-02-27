package com.mndk.bte_tr.gui.option.types;

import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class BooleanOption extends ToggleableOption<Boolean> {

    public BooleanOption(Supplier<Boolean> getter, Consumer<Boolean> setter, String name) {
        super(getter, setter, false, true, true, name);
    }

    @Override
    public Boolean getNext(Boolean current) {
        return !current;
    }

    @Override
    public String getStringOf(Boolean value) {
        return value ? I18n.format("gui.yes") : I18n.format("gui.no");
    }
}
