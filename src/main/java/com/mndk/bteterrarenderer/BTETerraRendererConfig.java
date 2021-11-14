package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;
import com.mndk.bteterrarenderer.tms.TileMapService;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = BTETerraRenderer.MODID)
public class BTETerraRendererConfig {


    @Config.Name("do_render")
    @Config.Comment("Set to true/false to enable/disable the map overlay.")
    public static boolean doRender = true;


    @Config.Name("map_service_id")
    public static String mapServiceId = "osm";


    @Config.Name("render_settings")
    @Config.Comment("General render settings")
    public static final RenderSettings RENDER_SETTINGS = new RenderSettings();
    @Getter @Setter
    public static final class RenderSettings {

        @Config.Name("align_x")
        @Config.Comment("The amount of which the map is offset along the X-axis")
        public double xAlign = 0;

        @Config.Name("align_z")
        @Config.Comment("The amount of which the map is offset along the Z-axis")
        public double zAlign = 0;

        @Config.Name("y_axis")
        @Config.Comment("The y-coordinate at which the map is rendered")
        public double yAxis = 4;

        @Config.Name("opacity")
        @Config.Comment("The opacity of the map")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double opacity = 0.7;

        @Config.Name("zoom")
        @Config.Comment("The higher the value is, the more the map's resolution will increase.")
        @Config.RangeInt(min = -3, max = 3)
        @Config.SlidingOption
        public int zoom = 0;

        @Config.Name("size")
        @Config.RangeInt(min = 1, max = 5)
        @Config.SlidingOption
        public int radius = 3;

        @Config.Name("y_diff_limit")
        @Config.Comment({
                "Limit of difference between the player's y-coordinate and the tile's y-coordinate.",
                "If the diff reaches the limit, then the map won't be rendered."
        })
        public double yDiffLimit = 1000;

    }


    @Config.Name("ui_settings")
    public static final UISettings UI_SETTINGS = new UISettings();
    @Getter @Setter
    public static class UISettings {

        @Config.Name("sidebar_side")
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @Config.Name("sidebar_width")
        @Config.RangeInt(min = 130)
        // It's not necessary for the sidebar to fill more than a half of the game screen
        public int sidebarWidth = 200;

        @Config.Name("sidebar_opacity")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double sidebarOpacity = 0.5;

    }


    private static class ConfigDataCache {
        /*
         * I couldn't put this in the main class, so I made a subclass BTRConfig.ConfigDataCache and put the
         * tms variable here.
         */
        private static TileMapService tileMapService = TileMapYamlLoader.result.getTileMap(mapServiceId);
    }

    public static TileMapService getTileMapService() {
        return ConfigDataCache.tileMapService;
    }

    public static void setTileMapService(TileMapService service) {
        ConfigDataCache.tileMapService = service;
        mapServiceId = service.getId();
    }

    private static void refreshTileMapService() {
        ConfigDataCache.tileMapService = TileMapYamlLoader.result.getTileMap(mapServiceId);
    }


    public static void save() {
        ConfigManager.sync(BTETerraRenderer.MODID, Config.Type.INSTANCE);
        refreshTileMapService();
    }



    @Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID)
    public static class ConfigEventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(BTETerraRenderer.MODID)) {
                BTETerraRendererConfig.save();
            }
        }

    }



}
