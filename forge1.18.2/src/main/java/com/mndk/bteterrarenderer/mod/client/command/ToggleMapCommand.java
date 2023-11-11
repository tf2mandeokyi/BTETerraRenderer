package com.mndk.bteterrarenderer.mod.client.command;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ToggleMapCommand implements Command<CommandSourceStack> {

    private static final ToggleMapCommand COMMAND = new ToggleMapCommand();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("togglebtrmap").executes(COMMAND);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        BTETerraRendererConfig.INSTANCE.toggleRender();
        return 0;
    }
}
