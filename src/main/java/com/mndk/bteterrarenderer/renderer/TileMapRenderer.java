package com.mndk.bteterrarenderer.renderer;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.storage.TileMapCache;
import com.mndk.bteterrarenderer.tms.TileMapService;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

public class TileMapRenderer {

	/**
	 * Renders tile map.
	 * @param tms TileMapService class
	 * @param px Player's x position
	 * @param py Player's y position
	 * @param pz Player's z position
	 */
	public static void renderTiles(TileMapService tms, double px, double py, double pz) {
		
		if(tms == null) return;

		BTETerraRendererConfig.RenderSettings settings = BTETerraRendererConfig.RENDER_SETTINGS;

		if(settings.yAxis <= py - settings.yDiffLimit || settings.yAxis >=py + settings.yDiffLimit) {
			return;
		}

		Tessellator t = Tessellator.getInstance();
		BufferBuilder builder = t.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.scale(1, 1, 1);

		int size = settings.radius - 1;
		
		// Iterate tiles around player
		for (int y = -size; y <= size; y++) for (int x = -size; x <= size; x++) {
			tms.renderTile(
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
