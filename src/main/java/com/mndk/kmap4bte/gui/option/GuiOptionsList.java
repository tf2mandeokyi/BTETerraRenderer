package com.mndk.kmap4bte.gui.option;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class GuiOptionsList {

    GuiScreen parent;
    public List<GuiOptionButton> buttons;
    int x, y, width, buttonHeight, buttonMarginTop;

    public GuiOptionsList(GuiScreen parent, int x, int y, int width, int buttonHeight, int buttonMarginTop) {
        buttons = new ArrayList<>();
        this.parent = parent;
        this.x = x; this.y = y;
        this.width = width; this.buttonHeight = buttonHeight; this.buttonMarginTop = buttonMarginTop;
    }

    public <T> void add(GuiOption<T> option) {
        int index = buttons.size();
        this.buttons.add(new GuiOptionButton<>(
                - buttons.size() - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                width, buttonHeight,
                option
        ));
    }

    public void actionPerformed(GuiButton button) {
        for(GuiOptionButton comp : this.buttons) {
            if(comp == button) comp.toggle();
        }
    }

    private static class GuiOptionButton<T> extends GuiButton {

        protected final GuiOption<T> option;

        public GuiOptionButton(int buttonId, int x, int y, int width, int height, GuiOption<T> option) {
            super(buttonId, x, y, width, height, "");
            this.option = option;
            this.updateDisplayString(option.get());
        }

        public void toggle() {
            T t = option.toggle();
            this.updateDisplayString(t);
        }

        public void updateDisplayString(T t) {
            this.displayString = option.name + ": " + t;
        }
    }

}
