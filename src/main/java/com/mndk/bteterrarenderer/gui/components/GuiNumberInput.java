package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.util.GetterSetter;
import com.mndk.bteterrarenderer.util.StringToNumber;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * Number input field class.
 */
public class GuiNumberInput extends GuiTextField {



	protected final GetterSetter<Double> value;
	protected final FontRenderer fontRenderer;
	protected final String prefix;
	protected int xPos;
	protected boolean numberValidated = true;



	public GuiNumberInput(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height, GetterSetter<Double> value, String prefix) {
		super(componentId, fontRenderer, x + fontRenderer.getStringWidth(prefix) + 5, y, width - fontRenderer.getStringWidth(prefix) - 5, height);
		this.xPos = x;
		this.value = value;
		this.setText(StringToNumber.formatNicely(value.get()));
		this.setMaxStringLength(50);
		this.fontRenderer = fontRenderer;
		this.prefix = prefix;
	}



	public void setWidth(int newWidth) {
		this.width = newWidth - fontRenderer.getStringWidth(prefix) - 5;
	}

	public void setX(int newX) {
		this.xPos = newX;
		this.x = newX + fontRenderer.getStringWidth(prefix) + 5;
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
			value.set(Double.parseDouble(s)); // Seems that there's no other method than refreshing it at every frame :/
		}

		super.drawString(fontRenderer, prefix,
				this.xPos, this.y + ((this.height - this.fontRenderer.FONT_HEIGHT) / 2),
				numberValidated ? 0xFFFFFF : 0xFF0000
		);
		super.drawTextBox();
	}
}