package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManagerImpl;
import net.minecraftforge.fml.common.Mod;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        // TODO: Separate server config and client config
        BTETerraRendererConfig.initialize(new ClientMinecraftManagerImpl());
        Loggers.get(this).info("Mod setup done");
    }
}
