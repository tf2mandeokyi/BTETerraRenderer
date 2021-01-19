package com.mndk.mapdisp4bte.proxy;

import com.mndk.mapdisp4bte.MapDisplayer4BTE;
import net.minecraftforge.fml.common.event.*;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MapDisplayer4BTE.logger = event.getModLog();
    }
    public void init(FMLInitializationEvent event) {}
    public void postInit(FMLPostInitializationEvent event) {}
    public void serverStarting(FMLServerStartingEvent event) {}
}
