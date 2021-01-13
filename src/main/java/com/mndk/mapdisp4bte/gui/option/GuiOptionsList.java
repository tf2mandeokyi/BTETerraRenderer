package com.mndk.mapdisp4bte.gui.option;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;

import java.util.ArrayList;
import java.util.List;

public class GuiOptionsList {

    GuiScreen parent;
    public List<GuiButton> buttons;
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
                -index-1,
                x, y + index * (buttonHeight + buttonMarginTop),
                width, buttonHeight,
                option
        ));
    }

    public void addSlider(GuiNumberOption<Float> option) {

        GuiPageButtonList.GuiResponder responder = new GuiPageButtonList.GuiResponder() {
            @Override
            public void setEntryValue(int id, float value) { option.set(value); }
            @Override public void setEntryValue(int id, boolean value) { }
            @Override public void setEntryValue(int id, String value) { }
        };

        GuiSlider.FormatHelper helper = (id, name, value) -> option.name + ": " + value;

        int index = buttons.size();

        GuiSlider tmp = new GuiSlider(
                responder,
                -index-1,
                x, y + index * (buttonHeight + buttonMarginTop),
                option.name,
                option.from, option.to,
                option.get(),
                helper
        );

        tmp.setWidth(width);

        this.buttons.add(tmp);
    }

    public void actionPerformed(GuiButton button) {
        for(GuiButton comp : this.buttons) {
            if(comp instanceof GuiOptionButton) {
                GuiOptionButton b = (GuiOptionButton) comp;
                if(b == button && b.option.isButton) b.toggle();
            }
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
            this.displayString = option.name + ": " + option.getStringOf(t);
        }
    }

}
