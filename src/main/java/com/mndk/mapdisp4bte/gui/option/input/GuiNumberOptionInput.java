package com.mndk.mapdisp4bte.gui.option.input;

import com.mndk.mapdisp4bte.gui.option.types.NumberOption;
import com.mndk.mapdisp4bte.util.StringToNumber;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiNumberOptionInput extends GuiTextField {

    protected final NumberOption<Double> option;
    protected final FontRenderer fontRenderer;
    protected boolean numberValidated = true;

    public GuiNumberOptionInput(int componentId, FontRenderer fontrenderer, int x, int y, int width, int height, NumberOption<Double> option) {
        super(componentId, fontrenderer, x, y, width, height);
        this.option = option;
        this.setText(StringToNumber.formatNicely(option.get()));
        this.setMaxStringLength(50);
        this.fontRenderer = fontrenderer;
    }

    @Override
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        boolean result = super.textboxKeyTyped(typedChar, keyCode);
        if(result) {
            String currentStr = this.getText();
            this.setTextColor((numberValidated = StringToNumber.validate(currentStr)) ? 0xFFFFFF : 0xFF0000);
        }
        return result;
    }

    @Override
    public void drawTextBox() {
        String s = this.getText();
        if(StringToNumber.validate(s)) {
            option.set(Double.parseDouble(s)); // Seems that there's no other method than refreshing it at every frame :/
        }
        super.drawString(fontRenderer, this.option.name,
                this.x - this.fontRenderer.getStringWidth(this.option.name) - 5,
                this.y + ((this.height - this.fontRenderer.FONT_HEIGHT) / 2),
                numberValidated ? 0xFFFFFF : 0xFF0000
        );
        super.drawTextBox();
    }
}