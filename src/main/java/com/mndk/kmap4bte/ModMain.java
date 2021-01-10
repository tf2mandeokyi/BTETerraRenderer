package com.mndk.kmap4bte;

import com.mndk.kmap4bte.commands.ModCommands;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModReference.MODID, name = ModReference.NAME, version = ModReference.VERSION)
public class ModMain {
    public static ModMain instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event) {
        ModCommands.registerCommands(event);
    }
}
