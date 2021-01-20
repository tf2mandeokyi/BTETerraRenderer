package com.mndk.mapdisp4bte.gui.option.toggleable;


import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class GuiNumberToggleable<N extends Number> extends GuiToggleable<N> {

    public GuiNumberToggleable(Supplier<N> getter, Consumer<N> setter, N from, N to, String name) {
        super(getter, setter, from, to, false, name);
    }

    @Override
    public N getNext(N current) {
        return null;
    }
}
