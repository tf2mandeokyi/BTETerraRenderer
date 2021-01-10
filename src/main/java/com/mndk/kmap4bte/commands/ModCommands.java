package com.mndk.kmap4bte.commands;

import net.minecraft.command.CommandBase;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.ArrayList;
import java.util.List;

public class ModCommands {

    private static final List<CommandBase> commands = new ArrayList<CommandBase>() {{
        add(new TestCommand());
    }};

    public static void registerCommands(FMLServerStartingEvent event) {
        for(CommandBase base : commands) {
            event.registerServerCommand(base);
        }
    }

}
