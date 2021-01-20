package com.mndk.mapdisp4bte.gui.toggleable;

import com.mndk.mapdisp4bte.util.TranslatableEnum;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiEnumToggleable<T extends TranslatableEnum<T>> extends GuiToggleable<T> {

    private final T[] list;

    public GuiEnumToggleable(Supplier<T> getter, Consumer<T> setter, T[] list, String name) {
        super(getter, setter, null, null, true, name);
        this.list = list;
    }

    @Override
    public T getNext(T current) {
        for(int i=0;i<list.length-1;i++) {
            if(list[i] != current) continue;
            return list[i+1];
        }
        return list[0];
    }

    @Override
    public String getStringOf(T value) {
        return value.getTranslatedString();
    }

}
