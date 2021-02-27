package com.mndk.bte_tr.gui.option.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mndk.bte_tr.util.TranslatableEnum;

@Deprecated
public class EnumOption<T extends TranslatableEnum<T>> extends ListOption<T> {

    public final T[] list;

    public EnumOption(Supplier<T> getter, Consumer<T> setter, T[] list, String name) {
        super(getter, setter, list, name);
        this.list = list;
    }

    @Override
    public String getStringOf(T value) {
        return value.getTranslatedString();
    }

}
