package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.config.BTRConfig;
import com.mndk.bteterrarenderer.renderer.TileMapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Side.CLIENT)
public class RenderEvent {



	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onRenderEvent(final RenderWorldLastEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;

		// "Smooth" player position
		final float partialTicks = event.getPartialTicks();
		final double px = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		final double py = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		final double pz = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		if(BTRConfig.doRender) {
			try {
				TileMapRenderer.renderTiles(BTRConfig.getTileMapService(), px, py, pz);
			} catch(IllegalArgumentException exception) {
				exception.printStackTrace();
			}
		}
	}


}
