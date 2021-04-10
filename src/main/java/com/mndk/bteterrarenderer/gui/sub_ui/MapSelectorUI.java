package com.mndk.bteterrarenderer.gui.sub_ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.config.ConfigHandler;
import com.mndk.bteterrarenderer.config.ModConfig;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsUI;
import com.mndk.bteterrarenderer.gui.util.ImageUiRenderer;
import com.mndk.bteterrarenderer.map.ExternalTileMap;
import com.mndk.bteterrarenderer.map.TileMapYamlLoader;
import com.mndk.bteterrarenderer.map.TileMapLoaderResult;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class MapSelectorUI extends GuiSubScreen {

	static final int COMPONENT_ID_GROUP = 200;
	
	private static List<Object> clickableElementList = new ArrayList<>();
	
	private static final int TITLE_MARGIN_BOTTOM = 20;
	
	private static int LIST_WIDTH;
	private static final int LIST_PADDING = 5;
	
	private static final int LIST_TOP_MARGIN = 40;
	private static final int ELEMENT_TOP_MARGIN = 10;
	private static final int LIST_LEFT_MARGIN = 30;
	private static final int LIST_LEFT = DefaultMapRenderingOptionsUI.OPTIONS_WIDTH + DefaultMapRenderingOptionsUI.LOPTIONS_MARGIN_LEFT
			+ LIST_LEFT_MARGIN;

	
	
	private static final ResourceLocation RADIO_BUTTON_IMAGE =
			new ResourceLocation(BTETerraRenderer.MODID, "textures/ui/radio_button.png");
	
	
	
	
	public MapSelectorUI(MapRenderingOptionsUI parent) {
		super(parent);
	}
	
	
	
	@Override
	protected void init() {
		
		int tempWidth;
		LIST_WIDTH = 0;
		clickableElementList.clear();
		
		for(TileMapLoaderResult.Category category : TileMapYamlLoader.result.getCategories()) {
			clickableElementList.add(category.getName());
			for(ExternalTileMap map : category.getMaps()) {
				clickableElementList.add(map);
				tempWidth = this.fontRenderer.getStringWidth(map.getName());
				LIST_WIDTH = LIST_WIDTH < tempWidth ? tempWidth : LIST_WIDTH;
			}
		}
		
		LIST_WIDTH += Math.max(120, LIST_WIDTH + 2 * LIST_PADDING + 20);
	}
	
	
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException { }
	
	
	
	@Override
	public void updateScreen() { }
	
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		int c = LIST_TOP_MARGIN + LIST_PADDING + this.fontRenderer.FONT_HEIGHT + TITLE_MARGIN_BOTTOM;
		int h = this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN;
		
		Gui.drawRect(LIST_LEFT, LIST_TOP_MARGIN, LIST_LEFT + LIST_WIDTH, c + h * clickableElementList.size(), 0x3F000000);
		
		this.drawCenteredString(
				this.fontRenderer, I18n.format("gui.bteterrarenderer.maprenderer.map_source"),
				LIST_LEFT + LIST_WIDTH / 2, LIST_TOP_MARGIN + LIST_PADDING, 0xFFFFFF
		);
		
		int i = 0;
		for(Object object : clickableElementList) {
			if(object instanceof String) {
				String categoryName = (String) object;
				this.drawCenteredString(this.fontRenderer, categoryName,
						LIST_LEFT + LIST_WIDTH / 2,
						c + h * i - (this.fontRenderer.FONT_HEIGHT / 2),
						0xFFFFFF
				);
			}
			else if(object instanceof ExternalTileMap) {
				
				ExternalTileMap map = (ExternalTileMap) object;
				float u = (ModConfig.currentMapManager.getId().equals(map.getId()) ? 1/8.f : 0) + (isMouseOnIndex(mouseX, mouseY, i) ? 1/16.f : 0);
				
				ImageUiRenderer.drawImage(RADIO_BUTTON_IMAGE,
						LIST_LEFT + LIST_PADDING,
						c + h * i - 8,
						0,
						16, 16,
						u, 0, u + 1/16.f, 1/16.f
				);
				this.drawString(this.fontRenderer, map.getName(),
						LIST_LEFT + LIST_PADDING + 20,
						c + h * i - (this.fontRenderer.FONT_HEIGHT / 2),
						0xFFFFFF
				);
			}
			++i;
		}
	}
	
	
	
	private boolean isMouseOnIndex(int mouseX, int mouseY, int index) {
		
		int c = LIST_TOP_MARGIN + LIST_PADDING + this.fontRenderer.FONT_HEIGHT + TITLE_MARGIN_BOTTOM;
		int h = this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN;
		
		int y = c + h * index;
		return
				mouseX >= LIST_LEFT + LIST_PADDING &&
				mouseX <= LIST_LEFT + LIST_WIDTH - LIST_PADDING &&
				mouseY >= y - 8 &&
				mouseY <= y + 8;
	}
	
	
	
	private int getMouseIndex(int mouseX, int mouseY) {
		if(mouseX < LIST_LEFT + LIST_PADDING || mouseX > LIST_LEFT + LIST_WIDTH - LIST_PADDING) return -1;
		
		int c = LIST_TOP_MARGIN + LIST_PADDING + this.fontRenderer.FONT_HEIGHT + TITLE_MARGIN_BOTTOM;
		int h = this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN;
		int index = (int) Math.round((mouseY - c) / (double) h), y = c + h * (int) index;
		
		if(index < 0 || index >= clickableElementList.size()) return -1;
		if(mouseY - y < -8 || mouseY - y > 8) return -1; // If the cursor is at the gap between elements
		return index;
	}
	
	
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int index = this.getMouseIndex(mouseX, mouseY);
		if(index == -1) return;
		Object obj = clickableElementList.get(index);
		if(!(obj instanceof ExternalTileMap)) return;
		ConfigHandler.getModConfig().setMapId(((ExternalTileMap) obj).getId());
	}
	
	
	
	@Override
	public void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException { }



	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	
	
}
