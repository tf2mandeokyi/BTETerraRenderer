package com.mndk.bteterrarenderer.core.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.json.TileMapServiceStateLoader;
import com.mndk.bteterrarenderer.core.loader.yml.FlatTileProjectionLoader;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceLoader;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.config.AbstractConfigSaveLoader;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class LoaderRegistry {
    private final TileMapServiceLoader TMS_YML = new TileMapServiceLoader(
            "maps", "assets/" + BTETerraRenderer.MODID + "/default_maps.yml"
    );
    private final FlatTileProjectionLoader FLAT_PROJ = new FlatTileProjectionLoader(
            "projections", "assets/" + BTETerraRenderer.MODID + "/default_projections.yml"
    );
    private AbstractConfigSaveLoader MOD_CONFIG;
    private TileMapServiceStateLoader TMS_STATES;

    // Generate getters and setters for each static members

    public TileMapService getCurrentTMS() {
        BTETerraRendererConfig.GeneralConfig config = BTETerraRendererConfig.GENERAL;
        return TMS_YML.getResult().getItem(config.mapServiceCategory, config.mapServiceId);
    }

    public TileMapServiceLoader tms() {
        return TMS_YML;
    }

    public FlatTileProjectionLoader flatProj() {
        return FLAT_PROJ;
    }

    public AbstractConfigSaveLoader modConfig() {
        return MOD_CONFIG;
    }

    public TileMapServiceStateLoader tmsStates() {
        return TMS_STATES;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setConfigDirectory(File configDirectory) {
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }

        FLAT_PROJ.refresh(configDirectory); // This should be called first after the TMS_YML.refresh
        TMS_YML.refresh(configDirectory);

        TMS_STATES = new TileMapServiceStateLoader(new File(configDirectory, "states.json"));
        TMS_STATES.load(TMS_YML.getResult());

        MOD_CONFIG = McConnector.common().newConfigSaveLoader(BTETerraRendererConfig.class, BTETerraRenderer.MODID);
        MOD_CONFIG.initialize();
        MOD_CONFIG.load();
    }

    public void save() {
        modConfig().save();
        tmsStates().save(tms().getResult());
    }

    public void load(boolean loadMapsOnly) {
        flatProj().refresh(); // This should be called first
        tms().refresh();
        tmsStates().load(tms().getResult());
        if (loadMapsOnly) return;

        modConfig().load();
    }
}
