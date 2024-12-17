package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
@SuppressWarnings("unused")
public class BTETerraRendererConfig {

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

        @Nullable
        @ConfigName("Projection JSON")
        @ConfigComment({
                "The projection JSON string for the map.",
                "Set this to null to use the default projection."
        })
        public String projectionJson = null;
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

    public void save() {
        ConfigLoaders.modConfig().save();
        ConfigLoaders.tmsStates().save(ConfigLoaders.tms().getResult());
        TileMapService.refreshSelectionFromConfig();
    }

    public void load(boolean loadMapsOnly) {
        ConfigLoaders.flatProj().refresh(); // This should be called first
        ConfigLoaders.tms().refresh();
        ConfigLoaders.tmsStates().load(ConfigLoaders.tms().getResult());
        TileMapService.refreshSelectionFromConfig();
        if (loadMapsOnly) return;

        ConfigLoaders.modConfig().load();
    }
}
