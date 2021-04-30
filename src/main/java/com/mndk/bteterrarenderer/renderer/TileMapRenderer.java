package com.mndk.bteterrarenderer.renderer;

import com.mndk.bteterrarenderer.config.ConfigHandler;
import com.mndk.bteterrarenderer.config.ModConfig;
import com.mndk.bteterrarenderer.tms.TileMapService;
import com.mndk.bteterrarenderer.storage.TileMapCache;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

public class TileMapRenderer {

	public static void renderTiles(TileMapService renderer, double px, double py, double pz) {
		
		if(renderer == null) return;
		
		Tessellator t = Tessellator.getInstance();
		BufferBuilder builder = t.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.scale(1, 1, 1);

		ModConfig config = ConfigHandler.getModConfig();

		int size = config.getRadius() - 1;
		
		// Iterate tiles around player
		for (int y = -size; y <= size; y++) for (int x = -size; x <= size; x++) {
			renderer.renderTile(
					t, builder,
					-config.getZoom(),
					config.getYLevel() + 0.1, (float) config.getOpacity(), // Adding .1 to y because rendering issue
					px+config.getXAlign(), py, pz+config.getZAlign(),
					x, y
			);
		}

		TileMapCache.getInstance().cleanup();

		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
