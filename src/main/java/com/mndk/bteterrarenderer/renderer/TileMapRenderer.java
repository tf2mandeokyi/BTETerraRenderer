package com.mndk.bteterrarenderer.renderer;

import com.mndk.bteterrarenderer.config.BTRConfig;
import com.mndk.bteterrarenderer.storage.TileMapCache;
import com.mndk.bteterrarenderer.tms.TileMapService;
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

		BTRConfig.RenderSettings settings = BTRConfig.RENDER_SETTINGS;

		int size = settings.radius - 1;
		
		// Iterate tiles around player
		for (int y = -size; y <= size; y++) for (int x = -size; x <= size; x++) {
			renderer.renderTile(
					t, builder,
					-settings.zoom,
					settings.yAxis + 0.1, (float) settings.opacity, // Adding .1 to y because rendering issue
					px + settings.align_x, py, pz + settings.align_z,
					x, y
			);
		}

		TileMapCache.getInstance().cleanup();

		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
