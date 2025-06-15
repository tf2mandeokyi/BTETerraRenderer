package com.mndk.bteterrarenderer.core;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.loader.LoaderRegistry;
import com.mndk.bteterrarenderer.core.projection.Proj4jProjection;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManager;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BTETerraRendererCore {
    public void initialize(ClientMinecraftManager clientManager) {
        McConnector.initialize(clientManager);

        Proj4jProjection.registerProjection();
        LoaderRegistry.setConfigDirectory(McConnector.common().getConfigDirectory(BTETerraRenderer.MODID));
    }
}
