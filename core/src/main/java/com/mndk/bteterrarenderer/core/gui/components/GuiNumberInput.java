package com.mndk.bteterrarenderer.core.gui.components;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import com.mndk.bteterrarenderer.core.util.input.InputKey;
import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.StringUtil;

/**
 * Number input field class.
 */
public class GuiNumberInput extends GuiAbstractWidgetCopy {

	private static final int PREFIX_BOX_DISTANCE = 3;

	private final GuiTextFieldCopy delegate;
	protected final PropertyAccessor<Double> value;
	protected int xPos;
	protected boolean numberValidated = true;

	public GuiNumberInput(int x, int y, int width, int height, PropertyAccessor<Double> value, String prefix) {
		super(x, y, width, height, prefix);
		this.delegate = new GuiTextFieldCopy(
				x + FontManager.getStringWidth(prefix) + PREFIX_BOX_DISTANCE, y,
				width - FontManager.getStringWidth(prefix) - PREFIX_BOX_DISTANCE, height
		);
		delegate.setText(StringUtil.formatDoubleNicely(value.get(), 3));
		delegate.setMaxStringLength(50);
		this.xPos = x;
		this.value = value;
	}

	@Override
	public void tick() {
		delegate.tick();
	}

	public void setWidth(int newWidth) {
		this.width = newWidth;
		delegate.width = newWidth - FontManager.getStringWidth(text) - PREFIX_BOX_DISTANCE;
	}

	public void setX(int newX) {
		this.xPos = newX;
		delegate.x = newX + FontManager.getStringWidth(text) + PREFIX_BOX_DISTANCE;
	}

	public boolean keyTyped(char typedChar, int keyCode) {
		boolean result = delegate.keyTyped(typedChar, keyCode);
		if(result) this.updateTextColor();
		return result;
	}

	public boolean keyPressed(InputKey key) {
		boolean result = delegate.keyPressed(key);
		if(result) this.updateTextColor();
		return result;
	}

	private void updateTextColor() {
		String currentStr = delegate.text;
		this.numberValidated = BtrUtil.validateDouble(currentStr);
		delegate.setTextColor(numberValidated ? NORMAL_TEXT_COLOR : ERROR_TEXT_COLOR);
		if(numberValidated) {
			value.set(Double.parseDouble(delegate.text));
		}
	}

	@Override
	public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
		return this.delegate.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
	}

	public void drawComponent(Object poseStack) {
		int fontHeight = FontManager.getFontHeight();

		int color = this.delegate.hovered ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
		if(this.delegate.isFocused()) color = FOCUSED_BORDER_COLOR;
		if(!numberValidated) color = ERROR_TEXT_COLOR;

		FontManager.drawStringWithShadow(poseStack,
				text,
				this.xPos, delegate.y + ((delegate.height - fontHeight) / 2f),
				color
		);
		delegate.drawComponent(poseStack);
	}

	public void update() {
		delegate.setText(StringUtil.formatDoubleNicely(value.get(), 3));
	}

	public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
		return delegate.mousePressed(mouseX, mouseY, mouseButton);
	}
}