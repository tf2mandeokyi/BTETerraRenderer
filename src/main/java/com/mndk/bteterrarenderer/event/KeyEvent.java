package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.config.BTRConfig;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsUI;
import com.mndk.bteterrarenderer.proxy.ClientProxy;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyEvent {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		
		if(ClientProxy.mapOptionsKey.isPressed()) {

			try { TileMapYamlLoader.refresh(); } catch (Exception e) {
				BTETerraRenderer.logger.error("Error caught while parsing yaml map files!");
				e.printStackTrace();
			}
			
			MapRenderingOptionsUI.open();
		}
		else if(ClientProxy.mapToggleKey.isPressed()) {
			BTRConfig.doRender = !BTRConfig.doRender;
			BTRConfig.save();
		}

		// TODO Delete these before the release
		if(ClientProxy.sidebarCheck.isPressed()) {
			Minecraft.getMinecraft().displayGuiScreen(new MapRenderingOptionsSidebar());
		}
	}

}
