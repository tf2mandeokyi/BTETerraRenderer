package com.mndk.mapdisp4bte.gui.option.slider;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiNumberSlider<T extends Number> {

    public Supplier<T> getter;
    public Consumer<T> setter;
    protected T from, to;
    public String name;

    public GuiNumberSlider(Supplier<T> getter, Consumer<T> setter, T from, T to, String name) {
        this.getter = getter; this.setter = setter;
        this.name = name;
        this.from = from; this.to = to;
    }

    public T getFrom() { return this.from; }
    public T getTo() { return this.to; }

    public String getStringOf(T value) {
        return value + "";
    }

    public T get() { return getter.get(); }
    public void set(T value) { setter.accept(value); }
}
