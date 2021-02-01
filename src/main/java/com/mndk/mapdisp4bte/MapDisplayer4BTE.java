package com.mndk.mapdisp4bte;

import com.mndk.mapdisp4bte.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

@Mod(modid = MapDisplayer4BTE.MODID, name = MapDisplayer4BTE.NAME, version = MapDisplayer4BTE.VERSION, clientSideOnly = true)
public class MapDisplayer4BTE {

    public static final String MODID = "mapdisp4bte";
    public static final String NAME = "MapDisplayer4BTE";
    public static final String VERSION = "1.01.3";

    public static Logger logger;

    @SidedProxy(clientSide="com.mndk.mapdisp4bte.proxy.ClientProxy", serverSide="com.mndk.mapdisp4bte.proxy.CommonProxy")
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
