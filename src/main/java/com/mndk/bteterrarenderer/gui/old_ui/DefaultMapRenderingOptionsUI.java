package com.mndk.bteterrarenderer.gui.old_ui;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;

/**
 * UI for default map rendering options.
 */
public class DefaultMapRenderingOptionsUI extends GuiSubScreen {
	
	static final int COMPONENT_ID_GROUP = 100;
	
	static final int VERTICAL_SHIFT = 0;
	static final int LOPTIONS_MARGIN_LEFT = 30;
	static final int LOPTIONS_COMPONENT_COUNT = 8;
	static final int LOPTIONS_MARGIN_BOTTOM = 6;
	static final int OPTIONS_WIDTH = 130;
	
	static final int DONE_BUTTON_MARGIN_BOTTOM = 26;
	
	
	
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
				I18n.format("gui.bteterrarenderer.settings.map_rendering") + ": " +
						(BTETerraRendererConfig.isDoRender() ?
								I18n.format("gui.bteterrarenderer.settings.enabled") :
								I18n.format("gui.bteterrarenderer.settings.disabled")
						)
		));
		
		
		// Map selector button
		++i;
		parent.addButton(this.mapSelectorToggler = new GuiButton(
				COMPONENT_ID_GROUP + i, 
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)), 
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bteterrarenderer.settings.change_map_source")
		));

		
		// Map Y axis input
		++i;
		this.mapYAxisInput = new GuiNumberInput(
				COMPONENT_ID_GROUP + i, this.fontRenderer, 
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)),
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT,
				GetterSetter.from(
						BTETerraRendererConfig.RENDER_SETTINGS::getYAxis,
						BTETerraRendererConfig.RENDER_SETTINGS::setYAxis
				),
				I18n.format("gui.bteterrarenderer.settings.map_y_level") + ": "
		);

		
		// Map opacity slider
		++i;
		parent.addButton(new GuiSlider(
				COMPONENT_ID_GROUP + i,
				LOPTIONS_MARGIN_LEFT, (int) (c + h * (i - count)),
				OPTIONS_WIDTH, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT, 
				I18n.format("gui.bteterrarenderer.settings.opacity") + ": ", "",
				0, 1, BTETerraRendererConfig.RENDER_SETTINGS.getOpacity(),
				true, true,
				slider -> BTETerraRendererConfig.RENDER_SETTINGS.setOpacity(slider.getValue())
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
	public void actionPerformed(GuiButton button) {
		if(button.equals(doneButton)) {
			Minecraft.getMinecraft().player.closeScreen();
		}
		else if(button.equals(this.mapRenderingToggler)) {
			this.mapRenderingToggler.displayString = I18n.format("gui.bteterrarenderer.settings.map_rendering") + ": " +
					(BTETerraRendererConfig.toggleRender() ?
							I18n.format("gui.bteterrarenderer.settings.enabled") :
							I18n.format("gui.bteterrarenderer.settings.disabled")
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
	public void mouseClicked(int x, int y, int button) {
		this.mapYAxisInput.mouseClicked(x, y, button);
	}
	
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int c = (parent.height - MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT) / 2 + VERTICAL_SHIFT;
		int h = MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT + MapRenderingOptionsUI.DEFAULT_BUTTON_MARGIN_BOTTOM;
		double count = (LOPTIONS_COMPONENT_COUNT - 1) / 2.;

		parent.drawCenteredString(this.fontRenderer, I18n.format("gui.bteterrarenderer.settings.title"),
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