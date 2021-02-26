package com.mndk.bte_tr.gui.option.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListOption<T> extends ToggleableOption<T> {

    public final T[] list;

    public ListOption(Supplier<T> getter, Consumer<T> setter, T[] list, String name) {
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

}
