package com.mndk.bte_tr.gui.option.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ToggleableOption<T> extends TypeOption<T> {

    protected Supplier<T> getter;
    protected Consumer<T> setter;
    protected T from, to;
    public boolean isButton;

    public ToggleableOption(Supplier<T> getter, Consumer<T> setter, T from, T to, boolean isButton, String name) {
        super(getter, setter, name);
        this.from = from; this.to = to;
        this.isButton = isButton;
    }

    public T toggle() {
        T t = get(), t1;
        if(t.equals(to))
            t1 = from;
        else
            t1 = getNext(t);
        set(t1); return t1;
    }

    public T getFrom() { return this.from; }
    public T getTo() { return this.to; }


    public abstract T getNext(T current);

    public String getStringOf(T value) {
        return value + "";
    }
}
