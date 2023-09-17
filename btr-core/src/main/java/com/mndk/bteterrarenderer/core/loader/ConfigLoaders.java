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
            FlatTileProjectionYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY); // This should be called first
            TileMapServiceYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY);
            TileMapServicePropertyLoader.load(TileMapServiceYamlLoader.INSTANCE.getResult());
        } catch (Throwable t) {
            BTETerraRendererConstants.LOGGER.error("Error while parsing map yaml files", t);
        }
    }

    public static void loadAll() {
        try {
            TileMapServicePropertyLoader.save(TileMapServiceYamlLoader.INSTANCE.getResult());
            FlatTileProjectionYamlLoader.INSTANCE.refresh(); // This should be called first
            TileMapServiceYamlLoader.INSTANCE.refresh();
            TileMapServicePropertyLoader.load(TileMapServiceYamlLoader.INSTANCE.getResult());
        } catch (Throwable t) {
            BTETerraRendererConstants.LOGGER.error("Error while parsing map yaml files", t);
        }
    }

}
