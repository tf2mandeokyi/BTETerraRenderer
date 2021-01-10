package com.mndk.kmap4bte.commands;

import com.mndk.kmap4bte.gui.MapRenderingOptionsUI;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TestCommand extends CommandBase {
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "test";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Minecraft.getMinecraft().displayGuiScreen(new MapRenderingOptionsUI());
    }
}
