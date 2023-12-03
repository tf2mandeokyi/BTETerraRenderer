package com.mndk.bteterrarenderer.mod.client.command;

import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

@UtilityClass
public class CommandsRegisterer {
    public void register() {
        var toggleSubcommand = ClientCommandManager.literal("toggle")
                .executes(new ToggleRenderingCommand())
                .build();
        var btrCommand = ClientCommandManager.literal("btr").then(toggleSubcommand);
        ClientCommandManager.DISPATCHER.register(btrCommand);
    }
}
