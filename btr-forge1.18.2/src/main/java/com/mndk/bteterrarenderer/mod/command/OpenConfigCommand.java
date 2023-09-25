package com.mndk.bteterrarenderer.mod.command;

import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@Deprecated
public class OpenConfigCommand implements Command<CommandSourceStack> {

    private static final OpenConfigCommand COMMAND = new OpenConfigCommand();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        // TODO: make this work
        return Commands.literal("openbtrcfg").executes(COMMAND);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        MapRenderingOptionsSidebar.open();
        return 0;
    }
}
