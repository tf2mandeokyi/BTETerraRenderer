package com.mndk.mapdisp4bte.gui.option.toggleable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GuiToggleable<T> {

    protected Supplier<T> getter;
    protected Consumer<T> setter;
    protected T from, to;
    public String name;
    public boolean isButton;

    public GuiToggleable(Supplier<T> getter, Consumer<T> setter, T from, T to, boolean isButton, String name) {
        this.getter = getter; this.setter = setter;
        this.name = name;
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


    public T get() { return getter.get(); }
    public void set(T value) { setter.accept(value); }
}
