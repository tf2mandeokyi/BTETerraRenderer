package com.mndk.bteterrarenderer.mod.client.command;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mojang.brigadier.CommandDispatcher;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommandsSetup {

    public void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(ToggleMapCommand.register());
//        dispatcher.register(OpenConfigCommand18.register());
    }

    @SubscribeEvent
    public void clientLoad(RegisterClientCommandsEvent event) {
        registerClientCommands(event.getDispatcher());
    }

}
