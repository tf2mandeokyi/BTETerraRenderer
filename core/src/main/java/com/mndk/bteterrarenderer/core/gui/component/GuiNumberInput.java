package com.mndk.bteterrarenderer.core.gui.component;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.TextFieldWidgetCopy;
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
				x + FontRenderer.DEFAULT.getStringWidth(prefix) + PREFIX_BOX_DISTANCE, y,
				width - FontRenderer.DEFAULT.getStringWidth(prefix) - PREFIX_BOX_DISTANCE, height
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
		delegate.setWidth(newWidth - FontRenderer.DEFAULT.getStringWidth(text) - PREFIX_BOX_DISTANCE);
	}

	public void setX(int newX) {
		this.xPos = newX;
		delegate.setX(newX + FontRenderer.DEFAULT.getStringWidth(text) + PREFIX_BOX_DISTANCE);
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
		String currentStr = delegate.getText();
		this.numberValidated = BTRUtil.validateDouble(currentStr);
		delegate.setTextColor(numberValidated ? NORMAL_TEXT_COLOR : ERROR_TEXT_COLOR);
		if(numberValidated) {
			value.set(Double.parseDouble(delegate.getText()));
		}
	}

	@Override
	public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
		return this.delegate.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
	}

	public void drawComponent(DrawContextWrapper drawContextWrapper) {
		int fontHeight = FontRenderer.DEFAULT.getHeight();

		int color = this.delegate.isHovered() ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
		if(this.delegate.isFocused()) color = FOCUSED_BORDER_COLOR;
		if(!numberValidated) color = ERROR_TEXT_COLOR;

		FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper,
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