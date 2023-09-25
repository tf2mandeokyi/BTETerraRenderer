package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;

public class BTETerraRendererMod implements ModInitializer {
    @Override
    public void onInitialize() {
        BTETerraRendererConstants.LOGGER.info("Mod BTETerraRenderer initialized");
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger("bteterrarenderer");
    }
}
