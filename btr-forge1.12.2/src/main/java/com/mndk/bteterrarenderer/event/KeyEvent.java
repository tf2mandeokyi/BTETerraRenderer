package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.connector.minecraft.ErrorMessageHandler;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.proxy.ClientProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRendererCore.MODID, value = Side.CLIENT)
public class KeyEvent {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		
		if(ClientProxy.mapOptionsKey.isPressed()) {

			try {
				ProjectionYamlLoader.INSTANCE.refresh();
				TMSYamlLoader.INSTANCE.refresh();
			} catch (Exception e) {
				ErrorMessageHandler.INSTANCE.sendToClient("Error caught while parsing yaml map files!", e);
			}

			MapRenderingOptionsSidebar.open();
		}
		else if(ClientProxy.mapToggleKey.isPressed()) {
			BTETerraRendererCore.CONFIG.toggleRender();
			BTETerraRendererCore.CONFIG.save();
		}
	}

}
