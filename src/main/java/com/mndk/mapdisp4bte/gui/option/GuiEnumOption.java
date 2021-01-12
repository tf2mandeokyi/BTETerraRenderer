package com.mndk.mapdisp4bte.gui.option;

import com.mndk.mapdisp4bte.util.IterableEnum;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiEnumOption<T extends IterableEnum<T>> extends GuiOption<T> {

    public GuiEnumOption(Supplier<T> getter, Consumer<T> setter, String name) {
        super(getter, setter, null, null, IterableEnum::next, true, name);
    }

    public T toggle() {
        T t = get().next();
        set(t); return t;
    }

}
