package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import lombok.experimental.UtilityClass;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Side.CLIENT)
public class KeyEvent {
	@SubscribeEvent
	public void onKeyEvent(InputEvent.KeyInputEvent event) {
		KeyBindings.checkInputs();
	}
}
