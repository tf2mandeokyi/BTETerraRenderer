package com.mndk.bteterrarenderer.commands;

import com.mndk.bteterrarenderer.config.ConfigHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class ToggleMapCommand extends CommandBase implements IClientCommand {

	@Override
	public void execute(MinecraftServer arg0, ICommandSender arg1, String[] arg2) throws CommandException {
		ConfigHandler.getModConfig().toggleTileRendering();
	}

	@Override
	public String getName() {
		return "togglebtrmap";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "togglebtrmap";
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender arg0, String arg1) {
		return false;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
