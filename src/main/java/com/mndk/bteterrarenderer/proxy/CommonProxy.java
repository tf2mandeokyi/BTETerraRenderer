package com.mndk.bteterrarenderer.proxy;

import com.mndk.bteterrarenderer.BTETerraRenderer;

import net.minecraftforge.fml.common.event.*;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		BTETerraRenderer.logger = event.getModLog();
	}
	public void init(FMLInitializationEvent event) {}
	public void postInit(FMLPostInitializationEvent event) {}
	public void serverStarting(FMLServerStartingEvent event) {}
}
