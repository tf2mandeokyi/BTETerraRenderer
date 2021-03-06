package com.mndk.bte_tr.gui.sub_ui;

import java.io.IOException;

import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.config.ModConfig;
import com.mndk.bte_tr.gui.MapRenderingOptionsUI;
import com.mndk.bte_tr.gui.util.ImageUiRenderer;
import com.mndk.bte_tr.map.ExternalTileMap;
import com.mndk.bte_tr.map.TileMapJsonLoader;
import com.mndk.bte_tr.map.TileMapJsonResult;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class MapSelectorUI extends GuiSubScreen {

	static final int COMPONENT_ID_GROUP = 200;
	
	private static final int TITLE_MARGIN_BOTTOM = 20;
	
	private static int LIST_WIDTH;
	private static final int LIST_PADDING = 5;
	
    private static final int LIST_TOP_MARGIN = 40;
    private static final int ELEMENT_TOP_MARGIN = 10;
    private static final int LIST_LEFT_MARGIN = 30;
    private static final int LIST_LEFT = DefaultMapRenderingOptionsUI.BASIC_OPTIONS_WIDTH + DefaultMapRenderingOptionsUI.BASIC_OPTIONS_MARGIN_LEFT
    		+ LIST_LEFT_MARGIN;

    // private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

    
    
    private static final ResourceLocation RADIO_BUTTON_IMAGE =
            new ResourceLocation(BTETerraRenderer.MODID, "textures/ui/radio_button.png");

    // private GuiButton doneButton;
    
    
	
	
	public MapSelectorUI(MapRenderingOptionsUI parent) {
		super(parent);
	}
	
	
	
	@Override
	protected void init() {
		
		int tempWidth;
		LIST_WIDTH = 0;
		for(TileMapJsonResult.Category category : TileMapJsonLoader.result.getCategories()) {
			for(ExternalTileMap map : category.getMaps()) {
				tempWidth = this.fontRenderer.getStringWidth(map.getName());
				LIST_WIDTH = LIST_WIDTH < tempWidth ? tempWidth : LIST_WIDTH;
			}
		}
		
		LIST_WIDTH += 2 * LIST_PADDING + 20;
		
		/*
		doneButton = new GuiButton(
				COMPONENT_ID_GROUP,
				LIST_LEFT + LIST_PADDING, parent.height - DONE_BUTTON_BOTTOM_MARGIN,
				LIST_WIDTH - 2 * LIST_PADDING, MapRenderingOptionsUI.DEFAULT_BUTTON_HEIGHT,
                I18n.format("gui.done")
        );
		*/
	}
	
	
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException { }
	
	
	
	@Override
	public void updateScreen() { }
	
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		int c = LIST_TOP_MARGIN + LIST_PADDING + this.fontRenderer.FONT_HEIGHT + TITLE_MARGIN_BOTTOM;
		int h = this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN;
		
		Gui.drawRect(LIST_LEFT, LIST_TOP_MARGIN, LIST_LEFT + LIST_WIDTH, c + h * 100, 0x3F000000); // TODO change 100 to something else
		
		this.drawCenteredString(
                this.fontRenderer, I18n.format("gui.bte_tr.maprenderer.map_source"),
                LIST_LEFT + LIST_WIDTH / 2, LIST_TOP_MARGIN + LIST_PADDING, 0xFFFFFF
        );
		
		int i = 0;
		for(TileMapJsonResult.Category category : TileMapJsonLoader.result.getCategories()) {
			for(ExternalTileMap map : category.getMaps()) {
	            float u = (ModConfig.currentMapManager.getId().equals(map.getId()) ? 1/8.f : 0) + (isMouseOnIndex(mouseX, mouseY, i) ? 1/16.f : 0);
	            ImageUiRenderer.drawImage(RADIO_BUTTON_IMAGE,
	            		LIST_LEFT + LIST_PADDING,
	                    c + h * i - 8,
	                    0,
	                    16, 16,
	                    u, 0, u + 1/16.f, 1/16.f);
	            this.drawString(this.fontRenderer, map.getName(),
	            		LIST_LEFT + LIST_PADDING + 20,
	                    c + h * i - (this.fontRenderer.FONT_HEIGHT / 2),
	                    0xFFFFFF);
	            i++;
			}
		}

        // doneButton.drawButton(parent.mc, mouseX, mouseY, partialTicks);
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
	
	
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int i = 0;
		for(TileMapJsonResult.Category category : TileMapJsonLoader.result.getCategories()) {
			for(ExternalTileMap map : category.getMaps()) {
	            if(isMouseOnIndex(mouseX, mouseY, i)) {
	                ConfigHandler.getModConfig().setMapId(map.getId());
	                return;
	            }
	            i++;
	        }
		}
	}
	
	
	
	@Override
	public void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException { }



	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	
	
}
