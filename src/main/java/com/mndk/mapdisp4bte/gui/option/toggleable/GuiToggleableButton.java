package com.mndk.mapdisp4bte.gui.option.toggleable;

import net.minecraft.client.gui.GuiButton;

public class GuiToggleableButton<T> extends GuiButton {

    public final ToggleableOption<T> option;

    public GuiToggleableButton(int buttonId, int x, int y, int width, int height, ToggleableOption<T> option) {
        super(buttonId, x, y, width, height, "");
        this.option = option;
        this.updateDisplayString(option.get());
    }

    public void toggle() {
        T t = option.toggle();
        this.updateDisplayString(t);
    }

    public void updateDisplayString(T t) {
        this.displayString = option.name + ": " + option.getStringOf(t);
    }
}