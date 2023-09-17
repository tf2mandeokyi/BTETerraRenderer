package com.mndk.bteterrarenderer.mod.commands;

import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.ParametersAreNonnullByDefault;

@Deprecated
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OpenConfigCommand12 extends CommandBase implements IClientCommand {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		// TODO: make this work
		MapRenderingOptionsSidebar.open();
	}

	@Override
	public String getName() {
		return "openbtrcfg";
	}

	@Override
	public String getUsage(ICommandSender var1) {
		return "openbtrcfg";
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender var1, String var2) {
		return false;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
