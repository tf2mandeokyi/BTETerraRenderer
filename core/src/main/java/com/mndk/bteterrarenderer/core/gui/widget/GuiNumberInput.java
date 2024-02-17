package com.mndk.bteterrarenderer.core.gui.widget;

import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.TextFieldWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.StringUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
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
				x + FontWrapper.DEFAULT.getWidth(prefix) + PREFIX_BOX_DISTANCE, y,
				width - FontWrapper.DEFAULT.getWidth(prefix) - PREFIX_BOX_DISTANCE, height
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
		delegate.setWidth(newWidth - FontWrapper.DEFAULT.getWidth(text) - PREFIX_BOX_DISTANCE);
	}

	public void setX(int newX) {
		this.xPos = newX;
		delegate.setX(newX + FontWrapper.DEFAULT.getWidth(text) + PREFIX_BOX_DISTANCE);
	}

	public boolean charTyped(char typedChar, int keyCode) {
		boolean result = delegate.charTyped(typedChar, keyCode);
		if(result) this.updateTextColor();
		return result;
	}

	public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
		boolean result = delegate.keyPressed(key, scanCode, modifiers);
		if(result) this.updateTextColor();
		return result;
	}

	private void updateTextColor() {
		String currentStr = delegate.getText();
		this.numberValidated = BTRUtil.validateDouble(currentStr);
		delegate.setTextColor(numberValidated ? NORMAL_TEXT_COLOR : ERROR_TEXT_COLOR);
		if(numberValidated) {
			value.set(Double.parseDouble(delegate.getText()));
		}
	}

	@Override
	public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
		return this.delegate.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
	}

	public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
		int fontHeight = FontWrapper.DEFAULT.getHeight();

		int color = this.delegate.isHovered() ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
		if(this.delegate.isFocused()) color = FOCUSED_BORDER_COLOR;
		if(!numberValidated) color = ERROR_TEXT_COLOR;

		drawContextWrapper.drawTextWithShadow(FontWrapper.DEFAULT,
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