package com.mndk.kmap4bte.gui.option;

import com.mndk.kmap4bte.util.IterableEnum;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiEnumOption<T extends IterableEnum<T>> extends GuiOption<T> {

    public GuiEnumOption(Supplier<T> getter, Consumer<T> setter, T from, T to, String name) {
        super(getter, setter, from, to, IterableEnum::next, name);
    }

}
