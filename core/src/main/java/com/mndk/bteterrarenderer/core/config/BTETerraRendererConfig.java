package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.yml.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.loader.json.TileMapServicePropertyLoader;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.projection.Proj4jProjection;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.mcconnector.config.AbstractConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
@SuppressWarnings("unused")
public class BTETerraRendererConfig {

    @ConfigIgnore
    public final AbstractConfigSaveLoader SAVE_LOADER_INSTANCE = AbstractConfigSaveLoader.makeSaveLoader(BTETerraRendererConfig.class);

    @ConfigName("General Settings")
    @ConfigComment({
            "General settings.",
            "Use this block to select which map to render."
    })
    public final GeneralConfig GENERAL = new GeneralConfig();
    @Getter @Setter @ConfigurableClass
    public class GeneralConfig {

        @ConfigName("Map Service Category")
        public String mapServiceCategory = "Global";

        @ConfigName("Map Service ID")
        public String mapServiceId = "osm";
    }

    @ConfigName("Hologram Settings")
    @ConfigComment({
            "Hologram settings.",
            "Contains hologram related settings. (duh)"
    })
    public final HologramConfig HOLOGRAM = new HologramConfig();
    @Getter @Setter @ConfigurableClass
    public class HologramConfig {

        @ConfigName("Do Render")
        @ConfigComment("Maps will be rendered if enabled")
        public boolean doRender = false;

        @ConfigName("X Align")
        @ConfigComment("The amount of which the map is offset on the X-axis")
        public double xAlign = 0.0;

        @ConfigName("Y Align")
        @ConfigComment("The amount of which the map is offset on the Y-axis.\nThis is only used for 3d maps.")
        public double yAlign = 0.0;

        @ConfigName("Z Align")
        @ConfigComment("The amount of which the map is offset on the Z-axis.")
        public double zAlign = 0.0;

        @ConfigName("Lock North")
        @ConfigComment("The map aligner direction will be locked to north if this is enabled.")
        public boolean lockNorth = false;

        @ConfigName("Flat Map Y Axis")
        @ConfigComment("The in-game Y-coordinate value at which the flat map is rendered.")
        public double flatMapYAxis = 4;

        @ConfigName("Opacity")
        @ConfigComment("The map opacity")
        @ConfigRangeDouble(min = 0, max = 1)
        @ConfigSlidingOption
        public double opacity = 0.7;

        @ConfigName("Y Diff Limit")
        @ConfigComment("Puts limit on how far the map is from the player to be rendered.")
        public double yDiffLimit = 1000;
    }

    @ConfigName("UI Settings")
    public final UIConfig UI = new UIConfig();
    @Getter @Setter @ConfigurableClass
    public class UIConfig {

        @ConfigName("Sidebar Side")
        @ConfigComment("Sets the location of the sidebar.")
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @ConfigName("Sidebar Width")
        @ConfigComment("Sets the width of the sidebar.")
        @ConfigRangeDouble(min = 180, max = 320)
        public double sidebarWidth = 200;

        @ConfigName("Sidebar Opacity")
        @ConfigComment("Sets the opacity of the sidebar.")
        @ConfigRangeDouble(min = 0, max = 1)
        @ConfigSlidingOption
        public double sidebarOpacity = 0.7;
    }

    public void toggleRender() {
        HOLOGRAM.setDoRender(!HOLOGRAM.isDoRender());
    }

    @ConfigIgnore
    private File MOD_CONFIG_DIRECTORY;
    public File getModConfigDirectory() { return MOD_CONFIG_DIRECTORY; }

    public void initialize(File gameConfigDirectory) {
        MOD_CONFIG_DIRECTORY = new File(gameConfigDirectory, BTETerraRendererConstants.MODID);

        // TMS data files
        Proj4jProjection.registerProjection();
        FlatTileProjectionYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY); // This should be called first
        TileMapServiceYamlLoader.INSTANCE.refresh(MOD_CONFIG_DIRECTORY);
        TileMapServicePropertyLoader.load(TileMapServiceYamlLoader.INSTANCE.getResult());

        // Config file
        SAVE_LOADER_INSTANCE.initialize();
        SAVE_LOADER_INSTANCE.load();
        refreshCurrentTileMapService();
    }

    public void save() {
        SAVE_LOADER_INSTANCE.save();
        TileMapServicePropertyLoader.save(TileMapServiceYamlLoader.INSTANCE.getResult());
        refreshCurrentTileMapService();
    }

    public void load(boolean loadMapsOnly) {
        FlatTileProjectionYamlLoader.INSTANCE.refresh(); // This should be called first
        TileMapServiceYamlLoader.INSTANCE.refresh();
        TileMapServicePropertyLoader.load(TileMapServiceYamlLoader.INSTANCE.getResult());
        if(loadMapsOnly) return;

        SAVE_LOADER_INSTANCE.load();
        refreshCurrentTileMapService();
    }

    @ConfigIgnore
    private CategoryMap.Wrapper<TileMapService<?>> TMS_ON_DISPLAY;
    public CategoryMap.Wrapper<TileMapService<?>> getTileMapServiceWrapper() {
        return TMS_ON_DISPLAY;
    }

    public void setTileMapService(CategoryMap.Wrapper<TileMapService<?>> wrapped) {
        TMS_ON_DISPLAY = wrapped;
        GENERAL.setMapServiceCategory(wrapped.getParentCategory().getName());
        GENERAL.setMapServiceId(wrapped.getId());
    }

    public void refreshCurrentTileMapService() {
        TMS_ON_DISPLAY = TileMapServiceYamlLoader.INSTANCE.getResult()
                .getItemWrapper(GENERAL.getMapServiceCategory(), GENERAL.getMapServiceId());
    }
}
