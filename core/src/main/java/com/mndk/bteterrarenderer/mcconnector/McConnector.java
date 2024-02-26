package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class McConnector {
    private static CommonMinecraftManager MINECRAFT;
    private static final Logger LOGGER = Loggers.get(McConnector.class);

    public static void initialize(CommonMinecraftManager minecraft) {
        if(MINECRAFT != null) LOGGER.warn("Minecraft reinitialization");
        LOGGER.info("initialization: " + minecraft.getClass());
        MINECRAFT = minecraft;
    }

    @Nonnull
    public static ClientMinecraftManager client() {
        try {
            return (ClientMinecraftManager) MINECRAFT;
        } catch(ClassCastException e) {
            throw new UnsupportedOperationException("Minecraft is not client", e);
        }
    }

    @Nonnull
    public static CommonMinecraftManager common() {
        return MINECRAFT;
    }
}
