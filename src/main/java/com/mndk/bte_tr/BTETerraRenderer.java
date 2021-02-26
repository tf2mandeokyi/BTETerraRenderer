package com.mndk.bte_tr;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

import com.mndk.bte_tr.proxy.CommonProxy;

@Mod(modid = BTETerraRenderer.MODID, name = BTETerraRenderer.NAME, version = BTETerraRenderer.VERSION, clientSideOnly = true)
public class BTETerraRenderer {

    public static final String MODID = "bte_tr";
    public static final String NAME = "BTETerraRenderer";
    public static final String VERSION = "1.01.3";

    public static Logger logger;

    @SidedProxy(clientSide="com.mndk.bte_tr.proxy.ClientProxy", serverSide="com.mndk.bte_tr.proxy.CommonProxy")
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
}
