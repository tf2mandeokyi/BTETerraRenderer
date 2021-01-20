package com.mndk.mapdisp4bte.gui.option.toggleable;


import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class GuiNumberToggleable extends GuiToggleable<Integer> {

    public GuiNumberToggleable(Supplier<Integer> getter, Consumer<Integer> setter, int min, int max, String name) {
        super(getter, setter, min, max, false, name);
    }

    @Override
    public Integer getNext(Integer current) {
        return current + 1;
    }
}
