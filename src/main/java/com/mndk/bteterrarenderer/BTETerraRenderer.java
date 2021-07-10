package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.network.ServerWelcomeMessage;
import com.mndk.bteterrarenderer.network.ServerWelcomeMsgHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import com.mndk.bteterrarenderer.proxy.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
		modid = BTETerraRenderer.MODID,
		name = BTETerraRenderer.NAME,
		dependencies = "required-after:terraplusplus;required-after:cubicchunks"
)
public class BTETerraRenderer {

	public static final String MODID = "bteterrarenderer";
	public static final String NAME = "BTETerraRenderer";

	public static Logger logger;

	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	@SidedProxy(clientSide="com.mndk.bteterrarenderer.proxy.ClientProxy", serverSide="com.mndk.bteterrarenderer.proxy.ServerProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		proxy.serverStarting(event);
	}

	static {
		NETWORK_WRAPPER.registerMessage(ServerWelcomeMsgHandler.class, ServerWelcomeMessage.class, 0, Side.CLIENT);
	}
}
