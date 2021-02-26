package com.mndk.bte_tr.gui.option.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NumberOption<T extends Number> {

    public Supplier<T> getter;
    public Consumer<T> setter;
    protected T min, max;
    public String name;

    public NumberOption(Supplier<T> getter, Consumer<T> setter, T from, T to, String name) {
        this.getter = getter; this.setter = setter;
        this.name = name;
        this.min = from; this.max = to;
    }

    public T getMin() { return this.min; }
    public T getMax() { return this.max; }

    public T get() { return getter.get(); }
    public void set(T value) { setter.accept(value); }
}
