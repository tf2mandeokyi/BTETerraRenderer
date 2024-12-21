package com.mndk.bteterrarenderer.mod.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class CommandsRegisterer implements ClientCommandRegistrationCallback {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(new CommandsRegisterer());
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        var toggleSubcommand = LiteralArgumentBuilder.<FabricClientCommandSource>literal("toggle")
                .executes(new ToggleRenderingCommand())
                .build();
        var btrCommand = ClientCommandManager.literal("btr").then(toggleSubcommand);
        dispatcher.register(btrCommand);
    }
}
