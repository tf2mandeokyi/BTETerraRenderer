package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.chat.ErrorMessageHandler;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.gui.old_ui.MapRenderingOptionsUI;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.proxy.ClientProxy;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Side.CLIENT)
public class KeyEvent {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		
		if(ClientProxy.mapOptionsKey.isPressed()) {

			try {
				ProjectionYamlLoader.INSTANCE.refresh();
				TMSYamlLoader.INSTANCE.refresh();
			} catch (Exception e) {
				ErrorMessageHandler.sendToClient("Error caught while parsing yaml map files!", e);
			}

			if(BTETerraRendererConfig.UI_SETTINGS.oldUi) {
				MapRenderingOptionsUI.open();
			} else {
				MapRenderingOptionsSidebar.open();
			}
		}
		else if(ClientProxy.mapToggleKey.isPressed()) {
			BTETerraRendererConfig.doRender = !BTETerraRendererConfig.doRender;
			BTETerraRendererConfig.save();
		}
	}

}
