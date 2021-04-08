package com.mndk.bteterrarenderer.gui.sub_ui;

import java.io.IOException;

import com.mndk.bteterrarenderer.config.ConfigHandler;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsUI;
import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.gui.components.NumberOption;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;

public class DefaultMapRenderingOptionsUI extends GuiSubScreen {
	
	static final int COMPONENT_ID_GROUP = 100;
	
	static final int VERTICAL_SHIFT = 0;
	static final int LOPTIONS_MARGIN_LEFT = 30;
	static final int LOPTIONS_COMPONENT_COUNT = 8;
	static final int LOPTIONS_MARGIN_BOTTOM = 6;
	static final int OPTIONS_WIDTH = 130;
	
	static final int DONE_BUTTON_MARGIN_BOTTOM = 26;
	
	
	
	static final ISlider OPACITY_SLIDER_RESPONDER = new ISlider() {
		@Override public void onChangeSliderValue(GuiSlider slider) {
			ConfigHandler.getModConfig().setOpacity(slider.getValue());
		}
	};
	
	
	
	GuiButton doneButton;
	GuiButton mapRenderingToggler;
	GuiButton mapSelectorToggler;
	GuiNumberInput mapYAxisInput;
	
	
	
	public DefaultMapRenderingOptionsUI(MapRenderingOptionsUI parent) {
		super(parent);
	}
	
	
	
	@Override
	protected void init() {
		
		int c = (parent.height - MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT) / 2 + VERTICAL_SHIFT;
		int h = MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT + LOPTIONS_MARGIN_BOTTOM;
		double count = (LOPTIONS_COMPONENT_COUNT - 1) / 2.;
		int i = 1;
		
		
		parent.addButton(this.mapRenderingToggler = new GuiButton(
				COMPONENT_ID_GROUP + i, 
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)), 
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bteterrarenderer.maprenderer.map_rendering") + ": " + 
						(ConfigHandler.getModConfig().isTileRendering() ? 
								I18n.format("gui.bteterrarenderer.maprenderer.enabled") : 
								I18n.format("gui.bteterrarenderer.maprenderer.disabled")
						)
		));
		
		
		// Map selector button
		++i;
		parent.addButton(this.mapSelectorToggler = new GuiButton(
				COMPONENT_ID_GROUP + i, 
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)), 
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bteterrarenderer.maprenderer.change_map_source")
		));

		
		// Map Y axis input
		++i;
		this.mapYAxisInput = new GuiNumberInput(
				COMPONENT_ID_GROUP + i, this.fontRenderer, 
				LOPTIONS_MARGIN_LEFT + OPTIONS_WIDTH / 3, (int) (c + h * (i - count)),
				OPTIONS_WIDTH * 2 / 3, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				new NumberOption<Double>(
						ConfigHandler.getModConfig()::getYLevel, 
						ConfigHandler.getModConfig()::setYLevel,
						-1000000.0, 1000000.0,
						I18n.format("gui.bteterrarenderer.maprenderer.map_y_level") + ": "
				)
		);

		
		// Map opacity slider
		++i;
		String opacitySliderName = I18n.format("gui.bteterrarenderer.maprenderer.opacity");
		parent.addButton(new GuiSlider(
				COMPONENT_ID_GROUP + i,
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)),
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				opacitySliderName + ": ", "",
				0, 1, ConfigHandler.getModConfig().getOpacity(),
				true, true,
				OPACITY_SLIDER_RESPONDER
		));

		
		// Done (or close) button
		i += 2;
		parent.addButton(this.doneButton = new GuiButton(
				0,
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)), 
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.done")
		));
	}
	
	
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if(button.equals(doneButton)) {
			Minecraft.getMinecraft().player.closeScreen();
		}
		else if(button.equals(this.mapRenderingToggler)) {
			ConfigHandler.getModConfig().toggleTileRendering();
			this.mapRenderingToggler.displayString = I18n.format("gui.bteterrarenderer.maprenderer.map_rendering") + ": " + 
					(ConfigHandler.getModConfig().isTileRendering() ? 
							I18n.format("gui.bteterrarenderer.maprenderer.enabled") : 
							I18n.format("gui.bteterrarenderer.maprenderer.disabled")
					);
		}
		else if(button.equals(mapSelectorToggler)) {
			parent.renderMapSelector = !parent.renderMapSelector;
		}
	}
	
	
	
	@Override
	public void keyTyped(char key, int keyCode) {
		this.mapYAxisInput.textboxKeyTyped(key, keyCode);
	}
	
	
	
	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		this.mapYAxisInput.mouseClicked(x, y, button);
	}
	
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int c = (parent.height - MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT) / 2 + VERTICAL_SHIFT;
		int h = MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT + MapRenderingOptionsUI.DEFAULT_BUTTON_MARGIN_BOTTOM;
		double count = (LOPTIONS_COMPONENT_COUNT - 1) / 2.;
		
		parent.drawCenteredString(this.fontRenderer, I18n.format("gui.bteterrarenderer.maprenderer.title"),
				LOPTIONS_MARGIN_LEFT + OPTIONS_WIDTH / 2, (int) (c - h * count) - this.fontRenderer.FONT_HEIGHT / 2,
				0xFFFFFF
		);
		
		this.mapYAxisInput.drawTextBox();
	}
	
	@Override
	public void updateScreen() {
		this.mapYAxisInput.updateCursorCounter();
	}



	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
}