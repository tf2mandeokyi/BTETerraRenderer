package com.mndk.mapdisp4bte.gui.option.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TypeOption<T> {

    protected Supplier<T> getter;
    protected Consumer<T> setter;
    public String name;

    public TypeOption(Supplier<T> getter, Consumer<T> setter, String name) {
        this.getter = getter; this.setter = setter;
        this.name = name;
    }

    public T get() { return getter.get(); }
    public void set(T value) { setter.accept(value); }
}
