package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class KeyEvent {
	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event) {
		KeyBindings.checkInputs();
	}
}
