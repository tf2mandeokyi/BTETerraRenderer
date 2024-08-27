package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManagerImpl;
import net.minecraftforge.fml.common.Mod;

@Mod(BTETerraRenderer.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        // TODO: Separate server config and client config
        BTETerraRenderer.initialize(new ClientMinecraftManagerImpl());
        Loggers.get(this).info("Mod setup done");
    }
}
