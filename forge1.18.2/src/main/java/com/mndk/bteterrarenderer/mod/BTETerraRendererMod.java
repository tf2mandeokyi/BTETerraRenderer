package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.util.Loggers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        File gameConfigDirectory = new File(Minecraft.getInstance().gameDirectory, "config");
        BTETerraRendererConfig.initialize(gameConfigDirectory);
        Loggers.get(this).info("Mod setup done");
    }
}
