package com.mndk.bteterrarenderer.mod;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.BTETerraRendererCore;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManagerImpl;
import net.minecraftforge.fml.common.Mod;

@Mod(BTETerraRenderer.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        // TODO: Separate server config and client config
        BTETerraRendererCore.initialize(new ClientMinecraftManagerImpl());
        Loggers.get(this).info("Mod setup done");
    }
}
