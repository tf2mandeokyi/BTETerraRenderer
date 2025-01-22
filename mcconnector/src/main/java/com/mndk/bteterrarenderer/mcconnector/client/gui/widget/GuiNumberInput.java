package com.mndk.bteterrarenderer.mcconnector.client.gui.widget;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.StringUtil;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import lombok.Setter;

import java.util.Optional;

/**
 * Number input field class.
 */
public class GuiNumberInput extends AbstractWidgetCopy {

	private static final int PREFIX_BOX_DISTANCE = 3;

	private final TextFieldWidgetCopy delegate;
	protected final PropertyAccessor<Double> value;
	protected int xPos;
	protected boolean numberValidated = true;
	@Setter
	protected Integer prefixColor = null;

	public GuiNumberInput(int x, int y, int width, int height, PropertyAccessor<Double> value, String prefix) {
		super(x, y, width, height, prefix);
		this.delegate = new TextFieldWidgetCopy(
				x + getDefaultFont().getWidth(prefix) + PREFIX_BOX_DISTANCE, y,
				width - getDefaultFont().getWidth(prefix) - PREFIX_BOX_DISTANCE, height
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
		delegate.setWidth(newWidth - getDefaultFont().getWidth(text) - PREFIX_BOX_DISTANCE);
	}

	public void setX(int newX) {
		this.xPos = newX;
		delegate.setX(newX + getDefaultFont().getWidth(text) + PREFIX_BOX_DISTANCE);
	}

	public boolean charTyped(char typedChar, int keyCode) {
		boolean result = delegate.charTyped(typedChar, keyCode);
		if (result) this.updateTextColor();
		return result;
	}

	public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
		boolean result = delegate.keyPressed(key, scanCode, modifiers);
		if (result) this.updateTextColor();
		return result;
	}

	private void updateTextColor() {
		String currentStr = delegate.getText();
		this.numberValidated = BTRUtil.validateDouble(currentStr);
		delegate.setTextColor(numberValidated ? NORMAL_TEXT_COLOR : ERROR_TEXT_COLOR);
		if (numberValidated) {
			value.set(Double.parseDouble(delegate.getText()));
		}
	}

	@Override
	public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
		return this.delegate.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
	}

	public void drawComponent(GuiDrawContextWrapper drawContextWrapper) {
		int fontHeight = getDefaultFont().getHeight();

		int color = this.delegate.isHovered() ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
		if (this.delegate.isFocused()) color = FOCUSED_BORDER_COLOR;
		if (!numberValidated) color = ERROR_TEXT_COLOR;

		drawContextWrapper.drawTextWithShadow(getDefaultFont(),
				text,
				this.xPos, delegate.getY() + ((delegate.getHeight() - fontHeight) / 2f),
				Optional.ofNullable(prefixColor).orElse(color)
		);
		delegate.drawComponent(drawContextWrapper);
	}

	public void update() {
		delegate.setText(StringUtil.formatDoubleNicely(value.get(), 3));
	}

	public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
		return delegate.mousePressed(mouseX, mouseY, mouseButton);
	}
}