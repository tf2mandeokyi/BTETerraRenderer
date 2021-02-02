package com.mndk.mapdisp4bte.gui.option.types;


import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class IntegerOption extends ToggleableOption<Integer> {

    public IntegerOption(Supplier<Integer> getter, Consumer<Integer> setter, int min, int max, String name) {
        super(getter, setter, min, max, false, name);
    }

    @Override
    public Integer getNext(Integer current) {
        return current + 1;
    }
}
