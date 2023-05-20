package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.proxy.ClientProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class KeyEvent {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		if(ClientProxy.mapOptionsKey.isPressed()) {
			BTRConfigConnector.refreshTileMapService();
			MapRenderingOptionsSidebar.open();
		}
		else if(ClientProxy.mapToggleKey.isPressed()) {
			BTRConfigConnector.INSTANCE.toggleRender();
			BTRConfigConnector.save();
		}
	}

}
