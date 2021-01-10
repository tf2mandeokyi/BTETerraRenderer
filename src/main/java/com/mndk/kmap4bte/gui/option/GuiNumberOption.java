package com.mndk.kmap4bte.gui.option;


import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiNumberOption<N extends Number> extends GuiOption<N> {

    public GuiNumberOption(Supplier<N> getter, Consumer<N> setter, N from, N to, String name) {
        super(getter, setter, from, to, null, false, name);
    }

}
