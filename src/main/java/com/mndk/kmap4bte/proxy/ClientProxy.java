package com.mndk.kmap4bte.proxy;

import com.mndk.kmap4bte.commands.ModCommands;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        ModCommands.registerCommands(event);
    }
}
