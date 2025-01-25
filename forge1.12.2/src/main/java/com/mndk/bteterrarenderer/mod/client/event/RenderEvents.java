package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.tile.RenderManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Side.CLIENT)
public class RenderEvents {
	@SubscribeEvent
	public void onWorldRender(final RenderWorldLastEvent event) {

		// "Smooth" player position
		final float partialTicks = event.getPartialTicks();
		EntityPlayer player = Minecraft.getMinecraft().player;
		final double px = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		final double py = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		final double pz = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		RenderManager.renderTiles(new WorldDrawContextWrapper() {}, px, py, pz);
	}

	@SubscribeEvent
	public void onHudRender(final RenderGameOverlayEvent.Post event) {
		RenderManager.renderHud(new GuiDrawContextWrapperImpl());
	}
}
