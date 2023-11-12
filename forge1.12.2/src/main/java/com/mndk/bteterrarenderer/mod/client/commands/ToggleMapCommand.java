package com.mndk.bteterrarenderer.mod.client.commands;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The command class that toggles the map rendering. client side only.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToggleMapCommand extends CommandBase implements IClientCommand {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		BTETerraRendererConfig.toggleRender();
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
