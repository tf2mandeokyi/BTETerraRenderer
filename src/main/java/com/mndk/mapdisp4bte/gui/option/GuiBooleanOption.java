package com.mndk.mapdisp4bte.gui.option;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiBooleanOption extends GuiOption<Boolean> {

    public GuiBooleanOption(Supplier<Boolean> getter, Consumer<Boolean> setter, String name) {
        super(getter, setter, false, true, b -> true, true, name);
    }

}
