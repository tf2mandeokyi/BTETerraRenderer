package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.util.Loggers;
import net.fabricmc.api.ModInitializer;

public class BTETerraRendererMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Loggers.get(this).info("Mod BTETerraRenderer initialized");
    }
}
