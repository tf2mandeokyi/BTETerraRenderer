package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.proxy.client.KeyMappings12;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class KeyEvent {
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		if(KeyMappings12.MAP_OPTIONS_KEY.isPressed()) {
			BTRConfigConnector.refreshTileMapService();
			MapRenderingOptionsSidebar.open();
		}
		else if(KeyMappings12.MAP_TOGGLE_KEY.isPressed()) {
			BTRConfigConnector.INSTANCE.toggleRender();
			BTRConfigConnector.save();
		}
	}
}
