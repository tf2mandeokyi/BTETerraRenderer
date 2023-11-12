package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        BTETerraRendererConfig.initialize();
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererMod.class);
    }
}
