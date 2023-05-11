package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.tile.TileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class RenderEvent {
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onRenderEvent(final RenderWorldLastEvent event) {
		if(!BTRConfigConnector.INSTANCE.isDoRender()) return;

		EntityPlayer player = Minecraft.getMinecraft().player;

		// "Smooth" player position
		final float partialTicks = event.getPartialTicks();
		final double px = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		final double py = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		final double pz = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		try {
			TileRenderer.renderTiles(px, py, pz);
		} catch(IllegalArgumentException exception) {
			exception.printStackTrace();
		}
	}
}
