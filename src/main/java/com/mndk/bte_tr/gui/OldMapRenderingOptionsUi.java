package com.mndk.bte_tr.gui;

import java.io.IOException;

import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.gui.option.GuiOptionsList;
import com.mndk.bte_tr.gui.option.types.BooleanOption;
import com.mndk.bte_tr.gui.option.types.EnumOption;
import com.mndk.bte_tr.gui.option.types.NumberOption;
import com.mndk.bte_tr.map.RenderMapSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

@Deprecated
public class OldMapRenderingOptionsUi extends GuiScreen {

	private static final int TITLE_HEIGHT = 8;
	private static final int OPTIONS_LIST_TOP_MARGIN = 24;
	private static final int BUTTON_TOP_MARGIN = 5;

	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

	private static final int SETTINGS_CENTER_X = 170;

	private static final int ALIGNMENT_IMAGE_MARGIN_BOTTOM = 20;
	private static final int ALIGNMENT_IMAGE_MARGIN_RIGHT = 20;
	private static final int ALIGNMENT_IMAGE_WIDTH = 128;
	private static final int ALIGNMENT_IMAGE_HEIGHT = 128;

	private static final int ALIGNMENT_RESET_BUTTON_WIDTH = 50;
	private static final int ALIGNMENT_RESET_BUTTON_HEIGHT = 20;

	private static final int MIN_IMAGE_ALIGNMENT_VALUE = -20;
	private static final int MAX_IMAGE_ALIGNMENT_VALUE = 20;
	private static final int IMAGE_ALIGNMENT_VALUE_RANGE = MAX_IMAGE_ALIGNMENT_VALUE - MIN_IMAGE_ALIGNMENT_VALUE;

	GuiButton doneButton, xAlignResetButton, zAlignResetButton;
	GuiOptionsList optionsList;

	private static final ResourceLocation ALIGNMENT_IMAGE_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
			"textures/ui/alignment_image.png");
	private static final ResourceLocation ALIGNMENT_MARKER_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
			"textures/ui/alignment_marker.png");

	private boolean mouseClickedInAlignmentImage = false;

	@Override
	public void initGui() {
		super.initGui();

		this.setupOptionsList();
		this.addOtherButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == doneButton) { // Done button
			Minecraft.getMinecraft().player.closeScreen();
		} else if (button == xAlignResetButton) {
			ConfigHandler.getModConfig().setXAlign(0);
		} else if (button == zAlignResetButton) {
			ConfigHandler.getModConfig().setZAlign(0);
		}
		optionsList.actionPerformed(button);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.optionsList.updateScreen();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawCenteredString(this.fontRenderer, I18n.format("gui.bte_tr.maprenderer.title"), SETTINGS_CENTER_X,
				TITLE_HEIGHT, 0xFFFFFF);

		this.optionsList.drawScreen(mouseX, mouseY, partialTicks);

		this.drawAlignmentImage();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.mouseClickedInAlignmentImage = this.isMouseInAlignmentImage(mouseX, mouseY)) {
			mouseXYToXZAlign(mouseX, mouseY);
		}
		this.optionsList.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.optionsList.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (mouseClickedInAlignmentImage) {
			if (this.isMouseInAlignmentImage(mouseX, mouseY)) {
				mouseXYToXZAlign(mouseX, mouseY);
			}
		}
	}

	private boolean isMouseInAlignmentImage(int mouseX, int mouseY) {
		return mouseX >= this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH
				&& mouseX <= this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT
				&& mouseY >= this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT
				&& mouseY <= this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM;
	}

	private void mouseXYToXZAlign(int mouseX, int mouseY) {
		int x1 = mouseX - this.width + ALIGNMENT_IMAGE_MARGIN_RIGHT + ALIGNMENT_IMAGE_WIDTH,
				y1 = mouseY - this.height + ALIGNMENT_IMAGE_MARGIN_BOTTOM + ALIGNMENT_IMAGE_HEIGHT;
		ConfigHandler.getModConfig().setXAlign(
				MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * x1 / (float) ALIGNMENT_IMAGE_WIDTH);
		ConfigHandler.getModConfig().setZAlign(
				MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * y1 / (float) ALIGNMENT_IMAGE_HEIGHT);
	}

	private void addOtherButtons() {
		this.addButton(doneButton = new GuiButton(0, SETTINGS_CENTER_X - (BUTTON_WIDTH / 2),
				this.height - DONE_BUTTON_BOTTOM_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("gui.done")));

		this.addButton(xAlignResetButton = new GuiButton(1000,
				this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_RESET_BUTTON_WIDTH,
				this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT - ALIGNMENT_RESET_BUTTON_HEIGHT
						- 5,
				ALIGNMENT_RESET_BUTTON_WIDTH, ALIGNMENT_RESET_BUTTON_HEIGHT,
				I18n.format("gui.bte_tr.maprenderer.x_align_reset")));

		this.addButton(zAlignResetButton = new GuiButton(1001,
				this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH - ALIGNMENT_RESET_BUTTON_WIDTH - 5,
				this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM
						- ((ALIGNMENT_IMAGE_HEIGHT + ALIGNMENT_RESET_BUTTON_HEIGHT) / 2),
				ALIGNMENT_RESET_BUTTON_WIDTH, ALIGNMENT_RESET_BUTTON_HEIGHT,
				I18n.format("gui.bte_tr.maprenderer.z_align_reset")));
	}

	private void setupOptionsList() {
		
		// Messy code smh
		
		this.optionsList = new GuiOptionsList(this, SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), OPTIONS_LIST_TOP_MARGIN,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_TOP_MARGIN);

		
		this.optionsList.addToggleableButton(new BooleanOption(ConfigHandler.getModConfig()::isTileRendering,
				ConfigHandler.getModConfig()::setTileRendering, I18n.format("gui.bte_tr.maprenderer.enable_render")));

		
		/*this.optionsList.addToggleableButton(
				new EnumOption<>(ConfigHandler.getModConfig()::getMapType, ConfigHandler.getModConfig()::setMapType,
						RenderMapType.values(), I18n.format("gui.bte_tr.maprenderer.map_type")));*/

		
		this.optionsList.addSelectionUiButton(
				new EnumOption<>(ConfigHandler.getModConfig()::getMapSource, ConfigHandler.getModConfig()::setMapSource,
						RenderMapSource.values(), I18n.format("gui.bte_tr.maprenderer.map_source")),
				I18n.format("gui.bte_tr.maprenderer.change_map_source"));

		
		this.optionsList.addNumberInput(
				new NumberOption<>(ConfigHandler.getModConfig()::getYLevel, ConfigHandler.getModConfig()::setYLevel,
						-1000000.0, 1000000.0, I18n.format("gui.bte_tr.maprenderer.map_y_level") + ": "),
				this.fontRenderer);

		
		this.optionsList.addSlider(new NumberOption<>(ConfigHandler.getModConfig()::getOpacity,
				ConfigHandler.getModConfig()::setOpacity, 0., 1., I18n.format("gui.bte_tr.maprenderer.opacity")));

		
		this.optionsList.addIntegerSlider(new NumberOption<>(ConfigHandler.getModConfig()::getZoom,
				ConfigHandler.getModConfig()::setZoom, -3, 3, I18n.format("gui.bte_tr.maprenderer.zoom")));
		
		
		this.optionsList.addIntegerSlider(new NumberOption<>(ConfigHandler.getModConfig()::getRadius,
				ConfigHandler.getModConfig()::setRadius, 0, 4, I18n.format("gui.bte_tr.maprenderer.size")));

		
		for (Gui component : optionsList.components) {
			if (component instanceof GuiButton) {
				this.addButton((GuiButton) component);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		try {
			ConfigHandler.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onGuiClosed();
	}

	private void drawAlignmentImage() {
		String xAlignString = I18n.format("gui.bte_tr.maprenderer.x_align") + ": "
				+ ConfigHandler.getModConfig().getXAlign() + "m";
		String zAlignString = I18n.format("gui.bte_tr.maprenderer.z_align") + ": "
				+ ConfigHandler.getModConfig().getZAlign() + "m";

		int imageRight = this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT, imageLeft = imageRight - ALIGNMENT_IMAGE_WIDTH;
		int imageBottom = this.height - ALIGNMENT_IMAGE_HEIGHT, imageTop = imageBottom - ALIGNMENT_IMAGE_MARGIN_BOTTOM;

		this.drawCenteredString(this.fontRenderer, I18n.format("gui.bte_tr.maprenderer.map_align"),
				imageLeft + (ALIGNMENT_IMAGE_WIDTH / 2),
				imageTop - ALIGNMENT_RESET_BUTTON_HEIGHT - this.fontRenderer.FONT_HEIGHT - 10, 0xFFFFFF);

		// X Align
		this.drawString(this.fontRenderer, xAlignString,
				imageRight - ALIGNMENT_RESET_BUTTON_WIDTH - this.fontRenderer.getStringWidth(xAlignString) - 5,
				imageTop - (this.fontRenderer.FONT_HEIGHT + ALIGNMENT_RESET_BUTTON_HEIGHT) / 2 - 5, 0xFFFFFF);

		// Z Align
		this.drawString(this.fontRenderer, zAlignString,
				imageLeft - ALIGNMENT_RESET_BUTTON_WIDTH - this.fontRenderer.getStringWidth(zAlignString) - 10,
				imageTop + ((ALIGNMENT_IMAGE_HEIGHT - this.fontRenderer.FONT_HEIGHT) / 2), 0xFFFFFF);

		// Alignment image
		ImageUiRenderer.drawImage(ALIGNMENT_IMAGE_RELOC, imageLeft, imageTop, this.zLevel, ALIGNMENT_IMAGE_WIDTH,
				ALIGNMENT_IMAGE_HEIGHT);

		// Alignment marker
		double x1 = -ALIGNMENT_IMAGE_WIDTH * (ConfigHandler.getModConfig().getXAlign() - MAX_IMAGE_ALIGNMENT_VALUE)
				/ IMAGE_ALIGNMENT_VALUE_RANGE,
				y1 = -ALIGNMENT_IMAGE_HEIGHT * (ConfigHandler.getModConfig().getZAlign() - MAX_IMAGE_ALIGNMENT_VALUE)
						/ IMAGE_ALIGNMENT_VALUE_RANGE;
		int marker_pos_x = (int) (x1 + this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH),
				marker_pos_y = (int) (y1 + this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT);

		// Alignment marker
		ImageUiRenderer.drawCenteredImage(ALIGNMENT_MARKER_RELOC, marker_pos_x, marker_pos_y, this.zLevel, 4, 4);
	}

}
