package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.mod.network.ServerWelcomeMessageImpl;
import com.mndk.bteterrarenderer.mod.network.ServerWelcomeMsgHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = BTETerraRenderer.MODID, name = BTETerraRenderer.NAME)
public class BTETerraRendererMod {

    public static final SimpleNetworkWrapper NETWORK_WRAPPER =
            NetworkRegistry.INSTANCE.newSimpleChannel(BTETerraRenderer.MODID);

    @SidedProxy(clientSide="com.mndk.bteterrarenderer.mod.client.ClientProxy", serverSide="com.mndk.bteterrarenderer.mod.server.ServerProxy")
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
        NETWORK_WRAPPER.registerMessage(ServerWelcomeMsgHandler.class, ServerWelcomeMessageImpl.class, 0, Side.CLIENT);
    }
}
