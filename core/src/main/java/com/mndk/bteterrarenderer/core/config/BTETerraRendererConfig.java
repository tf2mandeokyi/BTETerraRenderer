package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.config.annotation.*;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public class BTETerraRendererConfig {

    @ConfigIgnore
    public static final ConfigSaveLoader SAVE_LOADER_INSTANCE = ConfigSaveLoader.makeSaveLoader(BTETerraRendererConfig.class);

    @ConfigName("General Settings")
    @ConfigComment({
            "General settings.",
            "Use this block to select which map to render."
    })
    public static final GeneralConfig GENERAL = new GeneralConfig();
    @Getter @Setter @ConfigurableClass
    public static class GeneralConfig {

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
    public static final HologramConfig HOLOGRAM = new HologramConfig();
    @Getter @Setter @ConfigurableClass
    public static class HologramConfig {

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
    public static final UIConfig UI = new UIConfig();
    @Getter @Setter @ConfigurableClass
    public static class UIConfig {

        @ConfigName("Sidebar Side")
        @ConfigComment("Sets the location of the sidebar.")
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @ConfigName("Sidebar Width")
        @ConfigComment("Sets the width of the sidebar.")
        @ConfigRangeDouble(min = 130, max = 270)
        public double sidebarWidth = 200;

        @ConfigName("Sidebar Opacity")
        @ConfigComment("Sets the opacity of the sidebar.")
        @ConfigRangeDouble(min = 0, max = 1)
        @ConfigSlidingOption
        public double sidebarOpacity = 0.7;
    }

    public static void toggleRender() {
        HOLOGRAM.setDoRender(!HOLOGRAM.isDoRender());
    }

    public static void initialize() {
        SAVE_LOADER_INSTANCE.initialize();
    }

    public static void save() {
        BTETerraRendererConfig.SAVE_LOADER_INSTANCE.save();
        refreshCurrentTileMapService();
    }
    public static void load() {
        BTETerraRendererConfig.SAVE_LOADER_INSTANCE.load();
        refreshCurrentTileMapService();
    }

    @ConfigIgnore
    private static CategoryMap.Wrapper<TileMapService<?>> TMS_ON_DISPLAY;
    public static CategoryMap.Wrapper<TileMapService<?>> getTileMapServiceWrapper() {
        return TMS_ON_DISPLAY;
    }

    public static void setTileMapService(CategoryMap.Wrapper<TileMapService<?>> wrapped) {
        TMS_ON_DISPLAY = wrapped;
        GENERAL.setMapServiceCategory(wrapped.getParentCategory().getName());
        GENERAL.setMapServiceId(wrapped.getId());
    }

    public static void refreshCurrentTileMapService() {
        TMS_ON_DISPLAY = TileMapServiceYamlLoader.INSTANCE.getResult()
                .getItemWrapper(GENERAL.getMapServiceCategory(), GENERAL.getMapServiceId());
    }
}
