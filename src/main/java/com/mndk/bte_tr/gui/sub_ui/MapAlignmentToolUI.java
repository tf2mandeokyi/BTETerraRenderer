package com.mndk.bte_tr.gui.sub_ui;

import java.io.IOException;

import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.gui.MapRenderingOptionsUI;
import com.mndk.bte_tr.gui.util.ImageUiRenderer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class MapAlignmentToolUI extends GuiSubScreen {
	
	private static final int ALIGNMENT_IMAGE_MARGIN_BOTTOM = 20;
	private static final int ALIGNMENT_IMAGE_MARGIN_RIGHT = 20;
	private static final int ALIGNMENT_IMAGE_WIDTH = 128;
	private static final int ALIGNMENT_IMAGE_HEIGHT = 128;

	private static final int ALIGNMENT_RESET_BUTTON_WIDTH = 50;
	private static final int ALIGNMENT_RESET_BUTTON_HEIGHT = 20;

	private static final int MIN_IMAGE_ALIGNMENT_VALUE = -20;
	private static final int MAX_IMAGE_ALIGNMENT_VALUE = 20;
	private static final int IMAGE_ALIGNMENT_VALUE_RANGE = MAX_IMAGE_ALIGNMENT_VALUE - MIN_IMAGE_ALIGNMENT_VALUE;

	
	
	private static final ResourceLocation ALIGNMENT_IMAGE_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
			"textures/ui/alignment_image.png");
	private static final ResourceLocation ALIGNMENT_MARKER_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
			"textures/ui/alignment_marker.png");
	
	
	
	GuiButton xAlignResetButton, zAlignResetButton;
	private boolean mouseClickedInAlignmentImage = false;
	
	
	
	public MapAlignmentToolUI(MapRenderingOptionsUI parent) {
		super(parent);
	}



	@Override
	protected void init() {
		parent.addButton(xAlignResetButton = new GuiButton(1000,
				parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_RESET_BUTTON_WIDTH,
				parent.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT - ALIGNMENT_RESET_BUTTON_HEIGHT
						- 5,
				ALIGNMENT_RESET_BUTTON_WIDTH, ALIGNMENT_RESET_BUTTON_HEIGHT,
				I18n.format("gui.bte_tr.maprenderer.x_align_reset")));

		parent.addButton(zAlignResetButton = new GuiButton(1001,
				parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH - ALIGNMENT_RESET_BUTTON_WIDTH - 5,
				parent.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM
						- ((ALIGNMENT_IMAGE_HEIGHT + ALIGNMENT_RESET_BUTTON_HEIGHT) / 2),
				ALIGNMENT_RESET_BUTTON_WIDTH, ALIGNMENT_RESET_BUTTON_HEIGHT,
				I18n.format("gui.bte_tr.maprenderer.z_align_reset")));
	}



	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button == xAlignResetButton) {
			ConfigHandler.getModConfig().setXAlign(0);
		} else if (button == zAlignResetButton) {
			ConfigHandler.getModConfig().setZAlign(0);
		}
	}



	@Override
	public void updateScreen() {}



	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		String xAlignString = I18n.format("gui.bte_tr.maprenderer.x_align") + ": "
				+ ConfigHandler.getModConfig().getXAlign() + "m";
		String zAlignString = I18n.format("gui.bte_tr.maprenderer.z_align") + ": "
				+ ConfigHandler.getModConfig().getZAlign() + "m";

		int imageRight = parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT, imageLeft = imageRight - ALIGNMENT_IMAGE_WIDTH;
		int imageBottom = parent.height - ALIGNMENT_IMAGE_HEIGHT, imageTop = imageBottom - ALIGNMENT_IMAGE_MARGIN_BOTTOM;

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
		int marker_pos_x = (int) (x1 + parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH),
				marker_pos_y = (int) (y1 + parent.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT);

		// Alignment marker
		ImageUiRenderer.drawCenteredImage(ALIGNMENT_MARKER_RELOC, marker_pos_x, marker_pos_y, this.zLevel, 4, 4);
	}



	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.mouseClickedInAlignmentImage = this.isMouseInAlignmentImage(mouseX, mouseY)) {
			mouseXYToXZAlign(mouseX, mouseY);
		}
	}



	@Override
	public void keyTyped(char key, int keyCode) throws IOException {}



	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (mouseClickedInAlignmentImage) {
			if (this.isMouseInAlignmentImage(mouseX, mouseY)) {
				mouseXYToXZAlign(mouseX, mouseY);
			}
		}
	}
	
	
	
	private boolean isMouseInAlignmentImage(int mouseX, int mouseY) {
		return mouseX >= parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH
				&& mouseX <= parent.width - ALIGNMENT_IMAGE_MARGIN_RIGHT
				&& mouseY >= parent.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT
				&& mouseY <= parent.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM;
	}

	
	
	private void mouseXYToXZAlign(int mouseX, int mouseY) {
		int x1 = mouseX - parent.width + ALIGNMENT_IMAGE_MARGIN_RIGHT + ALIGNMENT_IMAGE_WIDTH,
				y1 = mouseY - parent.height + ALIGNMENT_IMAGE_MARGIN_BOTTOM + ALIGNMENT_IMAGE_HEIGHT;
		ConfigHandler.getModConfig().setXAlign(
				MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * x1 / (float) ALIGNMENT_IMAGE_WIDTH);
		ConfigHandler.getModConfig().setZAlign(
				MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * y1 / (float) ALIGNMENT_IMAGE_HEIGHT);
	}
	
}
