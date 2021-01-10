package com.mndk.kmap4bte.gui.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiOption<T> {

    protected Supplier<T> getter;
    protected Consumer<T> setter;
    protected Function<T, T> adder;
    protected T from, to;
    public String name;

    public GuiOption(Supplier<T> getter, Consumer<T> setter, T from, T to, Function<T, T> adder, String name) {
        this.getter = getter; this.setter = setter;
        this.name = name;
        this.adder = adder;
        this.from = from; this.to = to;
    }

    public T toggle() {
        T t = get(), t1;
        if(t.equals(to))
            t1 = from;
        else
            t1 = adder.apply(t);
        set(t1); return t1;
    }

    public T get() { return getter.get(); }
    public void set(T value) { setter.accept(value); }
}
