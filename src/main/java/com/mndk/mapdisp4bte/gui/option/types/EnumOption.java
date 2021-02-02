package com.mndk.mapdisp4bte.gui.option.types;

import com.mndk.mapdisp4bte.util.TranslatableEnum;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
