package com.mndk.bteterrarenderer.mod.client.command;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ToggleRenderingCommand implements Command<FabricClientCommandSource> {
    @Override
    public int run(CommandContext<FabricClientCommandSource> context) {
        BTETerraRendererConfig.toggleRender();
        return 1;
    }
}
