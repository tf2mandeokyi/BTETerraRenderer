package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.config.ConfigHandler;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsUI;
import com.mndk.bteterrarenderer.proxy.ClientProxy;

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
        	MapRenderingOptionsUI.open();
        }
        else if(ClientProxy.mapToggleKey.isPressed()) {
            ConfigHandler.getModConfig().toggleTileRendering();
        }
    }

}
