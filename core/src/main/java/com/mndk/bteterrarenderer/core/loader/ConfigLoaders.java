package com.mndk.bteterrarenderer.core.loader;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.json.TileMapServiceStatesLoader;
import com.mndk.bteterrarenderer.core.loader.yml.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.config.AbstractConfigSaveLoader;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class ConfigLoaders {
    private final TileMapServiceYamlLoader TMS_YML = new TileMapServiceYamlLoader(
            "maps", "assets/" + BTETerraRenderer.MODID + "/default_maps.yml"
    );
    private final FlatTileProjectionYamlLoader FLAT_PROJ = new FlatTileProjectionYamlLoader(
            "projections", "assets/" + BTETerraRenderer.MODID + "/default_projections.yml"
    );
    private AbstractConfigSaveLoader MOD_CONFIG;
    private TileMapServiceStatesLoader TMS_STATES;

    // Generate getters and setters for each static members

    public TileMapServiceYamlLoader tms() {
        return TMS_YML;
    }

    public FlatTileProjectionYamlLoader flatProj() {
        return FLAT_PROJ;
    }

    public AbstractConfigSaveLoader modConfig() {
        return MOD_CONFIG;
    }

    public TileMapServiceStatesLoader tmsStates() {
        return TMS_STATES;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setConfigDirectory(File configDirectory) {
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }

        FLAT_PROJ.refresh(configDirectory); // This should be called first after the TMS_YML.refresh
        TMS_YML.refresh(configDirectory);

        TMS_STATES = new TileMapServiceStatesLoader(new File(configDirectory, "states.json"));
        TMS_STATES.load(TMS_YML.getResult());

        MOD_CONFIG = McConnector.common().newConfigSaveLoader(BTETerraRendererConfig.class, BTETerraRenderer.MODID);
        MOD_CONFIG.initialize();
        MOD_CONFIG.load();
    }
}
