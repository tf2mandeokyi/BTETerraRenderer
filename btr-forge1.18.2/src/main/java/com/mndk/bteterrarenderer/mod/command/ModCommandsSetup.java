package com.mndk.bteterrarenderer.mod.command;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommandsSetup {

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(ToggleMapCommand18.register());
//        dispatcher.register(OpenConfigCommand18.register());
    }

    @SubscribeEvent
    public static void clientLoad(RegisterClientCommandsEvent event) {
        registerClientCommands(event.getDispatcher());
    }

}
