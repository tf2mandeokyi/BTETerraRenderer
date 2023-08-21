package com.mndk.bteterrarenderer.core.loader;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;

import java.io.File;

public class ConfigLoaders {

    public static File MOD_CONFIG_DIRECTORY;

    public static void loadAll(File gameConfigDirectory) {
        MOD_CONFIG_DIRECTORY = new File(gameConfigDirectory, BTETerraRendererConstants.MODID);
        try {
            BTETerraRendererConfig.INSTANCE.load();
            ProjectionYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY); // This should be called first
            TMSYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY);
            TMSPropertyLoader.load(TMSYamlLoader.INSTANCE.getResult());
        } catch (Exception e) {
            BTETerraRendererConstants.LOGGER.error("Error while parsing map yaml files", e);
        }
    }

    public static void loadAll() {
        try {
            TMSPropertyLoader.save(TMSYamlLoader.INSTANCE.getResult());
            ProjectionYamlLoader.INSTANCE.refresh(); // This should be called first
            TMSYamlLoader.INSTANCE.refresh();
            TMSPropertyLoader.load(TMSYamlLoader.INSTANCE.getResult());
        } catch (Exception e) {
            BTETerraRendererConstants.LOGGER.error("Error while parsing map yaml files", e);
        }
    }

}
