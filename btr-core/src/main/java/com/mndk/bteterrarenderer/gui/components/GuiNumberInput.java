package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.GetterSetter;
import com.mndk.bteterrarenderer.util.StringUtil;

/**
 * Number input field class.
 */
public class GuiNumberInput extends GuiAbstractWidgetImpl {

	private final GuiTextFieldImpl parent;
	protected final GetterSetter<Double> value;
	protected int xPos;
	protected boolean numberValidated = true;

	public GuiNumberInput(int x, int y, int width, int height, GetterSetter<Double> value, String prefix) {
		super(x, y, width, height, prefix);
		this.parent = new GuiTextFieldImpl(
				x + FontConnector.INSTANCE.getStringWidth(prefix) + 5, y,
				width - FontConnector.INSTANCE.getStringWidth(prefix) - 5, height
		);
		parent.setText(StringUtil.formatDoubleNicely(value.get(), 3));
		parent.setMaxStringLength(50);
		this.xPos = x;
		this.value = value;
	}


	public void setWidth(int newWidth) {
		this.width = newWidth;
		parent.width = newWidth - FontConnector.INSTANCE.getStringWidth(text) - 5;
	}


	public void setX(int newX) {
		this.xPos = newX;
		parent.x = newX + FontConnector.INSTANCE.getStringWidth(text) + 5;
	}


	public boolean keyTyped(char typedChar, int keyCode) {
		boolean result = parent.keyTyped(typedChar, keyCode);
		if(result) this.updateTextColor();
		return result;
	}


	public boolean keyPressed(InputKey key) {
		boolean result = parent.keyPressed(key);
		if(result) this.updateTextColor();
		return result;
	}


	private void updateTextColor() {
		String currentStr = parent.text;
		this.numberValidated = BtrUtil.validateDouble(currentStr);
		parent.setTextColor(numberValidated ? 0xFFFFFF : 0xFF0000);
		if(numberValidated) {
			value.set(Double.parseDouble(parent.text));
		}
	}


	public void drawComponent(double mouseX, double mouseY, float partialTicks) {
		int fontHeight = FontConnector.INSTANCE.getFontHeight();
		FontConnector.INSTANCE.drawStringWithShadow(text,
				this.xPos, parent.y + ((parent.height - fontHeight) / 2f),
				numberValidated ? 0xFFFFFF : 0xFF0000
		);
		parent.drawComponent(mouseX, mouseY, partialTicks);
	}


	public void update() {
		parent.setText(StringUtil.formatDoubleNicely(value.get(), 3));
	}

	public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
		return parent.mousePressed(mouseX, mouseY, mouseButton);
	}
}