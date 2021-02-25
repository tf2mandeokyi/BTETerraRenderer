package com.mndk.mapdisp4bte.gui.option;

import com.mndk.mapdisp4bte.gui.option.selection.GuiEnumSelectionUi;
import com.mndk.mapdisp4bte.gui.option.input.GuiNumberOptionInput;
import com.mndk.mapdisp4bte.gui.option.types.EnumOption;
import com.mndk.mapdisp4bte.gui.option.types.NumberOption;
import com.mndk.mapdisp4bte.gui.option.types.ToggleableOption;
import com.mndk.mapdisp4bte.gui.option.toggleable.GuiToggleableButton;
import com.mndk.mapdisp4bte.util.TranslatableEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiOptionsList {


    GuiScreen parent;
    public List<Gui> components;
    int x, y, width, buttonHeight, buttonMarginTop;


    public GuiOptionsList(GuiScreen parent, int x, int y, int width, int buttonHeight, int buttonMarginTop) {
        components = new ArrayList<>();
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.buttonHeight = buttonHeight;
        this.buttonMarginTop = buttonMarginTop;
    }


    public <T> void addToggleableButton(ToggleableOption<T> option) {
        int index = components.size();
        this.components.add(new GuiToggleableButton<>(
                -index - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                width, buttonHeight,
                option
        ));
    }


    public <T extends TranslatableEnum<T>> void addSelectionUiButton(EnumOption<T> option, String buttonText) {
        int index = components.size();
        this.components.add(new GuiSelectionUiButton<>(
                -index - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                width, buttonHeight,
                buttonText, option
        ));
    }


    public void addSlider(NumberOption<Double> option) {

        GuiPageButtonList.GuiResponder responder = new GuiPageButtonList.GuiResponder() {
            @Override public void setEntryValue(int id, float value) { option.set((double) value); }
            @Override public void setEntryValue(int id, boolean value) { }
            @Override public void setEntryValue(int id, String value) { }
        };

        GuiSlider.FormatHelper helper = (id, name, value) -> option.name + ": " + value;
        int index = components.size();
        
        GuiSlider tmp = new GuiSlider(
                responder,
                -index - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                option.name,
                option.getMin().floatValue(), option.getMax().floatValue(),
                option.get().floatValue(),
                helper
        );

        tmp.setWidth(width);
        this.components.add(tmp);
    }


    public void addSlider(Supplier<Double> getter, Consumer<Double> setter, double from, double to, String optionName) {

        GuiPageButtonList.GuiResponder responder = new GuiPageButtonList.GuiResponder() {
            @Override public void setEntryValue(int id, float value) { setter.accept((double) value); }
            @Override public void setEntryValue(int id, boolean value) { }
            @Override public void setEntryValue(int id, String value) { }
        };

        GuiSlider.FormatHelper helper = (id, name, value) -> optionName + ": " + value;
        int index = components.size();
        
        GuiSlider tmp = new GuiSlider(
                responder,
                -index - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                optionName,
                (float) from, (float) to,
                getter.get().floatValue(),
                helper
        );

        tmp.setWidth(width);
        this.components.add(tmp);
    }


    public void addSlider(Supplier<Integer> getter, Consumer<Integer> setter, int from, int to, String optionName) {

        GuiPageButtonList.GuiResponder responder = new GuiPageButtonList.GuiResponder() {
            @Override public void setEntryValue(int id, float value) { setter.accept((int) value); }
            @Override public void setEntryValue(int id, boolean value) { }
            @Override public void setEntryValue(int id, String value) { }
        };

        GuiSlider.FormatHelper helper = (id, name, value) -> optionName + ": " + (int) value;
        int index = components.size();
        
        GuiSlider tmp = new GuiSlider(
                responder,
                -index - 1,
                x, y + index * (buttonHeight + buttonMarginTop),
                optionName,
                (int) from, (int) to,
                getter.get().intValue(),
                helper
        );

        tmp.setWidth(width);
        this.components.add(tmp);
    }


    public void addNumberInput(NumberOption<Double> option, FontRenderer fontRenderer) {
        int index = components.size();

        GuiNumberOptionInput input = new GuiNumberOptionInput(
                -index - 1,
                fontRenderer,
                x + (width / 3), y + index * (buttonHeight + buttonMarginTop),
                width * 2 / 3, buttonHeight,
                option
        );

        this.components.add(input);
    }


    public void actionPerformed(GuiButton button) {
        for (Gui component : this.components) {
            if (component instanceof GuiToggleableButton<?>) {
                GuiToggleableButton<?> b = (GuiToggleableButton<?>) component;
                if (b == button && b.option.isButton) b.toggle();
            }
            else if(component instanceof GuiSelectionUiButton<?>) {
                GuiSelectionUiButton<?> b = (GuiSelectionUiButton<?>) component;
                if (b == button) Minecraft.getMinecraft().displayGuiScreen(b.getSelectionUi());
            }
        }
    }


    public void keyTyped(char c, int p) {
        for (Gui component : this.components) {
            if (component instanceof GuiTextField) {
                ((GuiTextField) component).textboxKeyTyped(c, p);
            }
        }
    }


    public void updateScreen() {
        for (Gui component : this.components) {
            if (component instanceof GuiTextField) {
                ((GuiTextField) component).updateCursorCounter();
            }
        }
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (Gui component : this.components) {
            if (component instanceof GuiTextField) {
                ((GuiTextField) component).drawTextBox();
            }
        }
    }


    public void mouseClicked(int x, int y, int button) {
        for (Gui component : this.components) {
            if (component instanceof GuiTextField) {
                ((GuiTextField) component).mouseClicked(x, y, button);
            }
        }
    }


    public static class GuiSelectionUiButton<T extends TranslatableEnum<T>> extends GuiButton {

        private final EnumOption<T> option;

        public GuiSelectionUiButton(int buttonId, int x, int y, int width, int height, String buttonText, EnumOption<T> option) {
            super(buttonId, x, y, width, height, buttonText);
            this.option = option;
        }

        public GuiEnumSelectionUi<T> getSelectionUi() {
            return new GuiEnumSelectionUi<>(this.option);
        }

    }
}
