package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import java.io.File;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        File gameConfigDirectory = new File(Minecraft.getInstance().gameDirectory, "config");
        BTETerraRendererConfig.initialize(gameConfigDirectory);
        BTETerraRendererConstants.LOGGER.info("Mod setup done");
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererMod.class);
    }
}
