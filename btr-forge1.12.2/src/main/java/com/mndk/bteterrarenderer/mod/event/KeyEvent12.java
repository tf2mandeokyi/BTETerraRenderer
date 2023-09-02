package com.mndk.bteterrarenderer.mod.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.mod.proxy.client.KeyMappings12;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class KeyEvent12 {
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		if(KeyMappings12.MAP_OPTIONS_KEY.isPressed()) {
			BTETerraRendererConfig.INSTANCE.refreshTileMapService();
			MapRenderingOptionsSidebar.open();
		}
		else if(KeyMappings12.MAP_TOGGLE_KEY.isPressed()) {
			BTETerraRendererConfig.INSTANCE.toggleRender();
			BTETerraRendererConfig.INSTANCE.save();
		}
	}
}
