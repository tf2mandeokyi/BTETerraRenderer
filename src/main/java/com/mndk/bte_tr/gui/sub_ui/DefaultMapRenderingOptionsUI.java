package com.mndk.bte_tr.gui.sub_ui;

import java.io.IOException;

import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.gui.MapRenderingOptionsUI;
import com.mndk.bte_tr.gui.components.GuiNumberInput;
import com.mndk.bte_tr.gui.components.NumberOption;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.resources.I18n;

public class DefaultMapRenderingOptionsUI extends GuiSubScreen {
	
	static final int COMPONENT_ID_GROUP = 100;
	
	static final int VERTICAL_SHIFT = 0;
	static final int BASIC_OPTIONS_MARGIN_LEFT = 50;
	static final int BASIC_OPTIONS_WIDTH = 150;
	static final int BASIC_OPTIONS_COMPONENT_COUNT = 7;
	static final int OPTIONS_MARGIN_BOTTOM = 7;
	
	static final int DONE_BUTTON_MARGIN_BOTTOM = 26;
	
	
	
	static final GuiPageButtonList.GuiResponder OPACITY_SLIDER_RESPONDER = new GuiPageButtonList.GuiResponder() {
		@Override public void setEntryValue(int arg0, float arg1) { ConfigHandler.getModConfig().setOpacity(arg1); }
		@Override public void setEntryValue(int arg0, boolean arg1) {}
		@Override public void setEntryValue(int arg0, String arg1) {}
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
		int h = MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT + OPTIONS_MARGIN_BOTTOM;
		double count = (BASIC_OPTIONS_COMPONENT_COUNT - 1) / 2.;
		
		parent.addButton(this.mapRenderingToggler = new GuiButton(
				COMPONENT_ID_GROUP + 1, 
				BASIC_OPTIONS_MARGIN_LEFT, (int) (c + h * (1 - count)), 
				BASIC_OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bte_tr.maprenderer.map_rendering") + ": " + 
						(ConfigHandler.getModConfig().isTileRendering() ? 
								I18n.format("gui.bte_tr.maprenderer.enabled") : 
								I18n.format("gui.bte_tr.maprenderer.disabled")
						)
		));
		
		parent.addButton(this.mapSelectorToggler = new GuiButton(
				COMPONENT_ID_GROUP + 2, 
				BASIC_OPTIONS_MARGIN_LEFT, (int) (c + h * (2 - count)), 
				BASIC_OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bte_tr.maprenderer.change_map_source")
		));
		
		this.mapYAxisInput = new GuiNumberInput(
				COMPONENT_ID_GROUP + 3, this.fontRenderer, 
				BASIC_OPTIONS_MARGIN_LEFT + BASIC_OPTIONS_WIDTH / 3, (int) (c + h * (3 - count)),
				BASIC_OPTIONS_WIDTH * 2 / 3, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				new NumberOption<Double>(
						ConfigHandler.getModConfig()::getYLevel, 
						ConfigHandler.getModConfig()::setYLevel,
						-1000000.0, 1000000.0,
						I18n.format("gui.bte_tr.maprenderer.map_y_level") + ": "
				)
		);
		
		String opacitySliderName = I18n.format("gui.bte_tr.maprenderer.opacity");
		parent.addButton(new GuiSlider(
				OPACITY_SLIDER_RESPONDER,
				COMPONENT_ID_GROUP + 4,
				BASIC_OPTIONS_MARGIN_LEFT, (int) (c + h * (4 - count)),
				opacitySliderName,
				0, 1,
				(float) ConfigHandler.getModConfig().getOpacity(),
				(id, name, value) -> opacitySliderName + ": " + value
		));
		
		parent.addButton(this.doneButton = new GuiButton(
				0,
				BASIC_OPTIONS_MARGIN_LEFT, (int) (c + h * (6 - count)), 
				BASIC_OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
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
			this.mapRenderingToggler.displayString = I18n.format("gui.bte_tr.maprenderer.map_rendering") + ": " + 
					(ConfigHandler.getModConfig().isTileRendering() ? 
							I18n.format("gui.bte_tr.maprenderer.enabled") : 
							I18n.format("gui.bte_tr.maprenderer.disabled")
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
		double count = (BASIC_OPTIONS_COMPONENT_COUNT - 1) / 2.;
		
		parent.drawCenteredString(this.fontRenderer, I18n.format("gui.bte_tr.maprenderer.title"),
				BASIC_OPTIONS_MARGIN_LEFT + BASIC_OPTIONS_WIDTH / 2, (int) (c - h * count) - this.fontRenderer.FONT_HEIGHT / 2,
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