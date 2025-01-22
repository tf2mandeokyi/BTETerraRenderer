package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.tile.TileRenderer;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.util.Loggers;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Side.CLIENT)
public class RenderEvents {
	@SubscribeEvent
	public void onRenderEvent(final RenderWorldLastEvent event) {

		// "Smooth" player position
		final float partialTicks = event.getPartialTicks();
		EntityPlayer player = Minecraft.getMinecraft().player;
		final double px = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		final double py = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		final double pz = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		try {
			// Since there's no "PoseStack" class in 1.12.2,
			// we'll just pass an empty instance here
			TileRenderer.renderTiles(new WorldDrawContextWrapper() {}, px, py, pz);
		} catch (IllegalArgumentException exception) {
			Loggers.get().error("Error while rendering tiles", exception);
		}
	}
}
