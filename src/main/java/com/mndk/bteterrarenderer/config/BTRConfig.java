package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;
import com.mndk.bteterrarenderer.tms.TileMapService;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = BTETerraRenderer.MODID)
public class BTRConfig {



    @Config.Name("do_render")
    @Config.Comment("Set to true/false to enable/disable the map overlay.")
    public static boolean doRender = true;



    @Config.Name("map_service_id")
    public static String mapServiceId = "osm";



    public static final class RenderSettings {

        @Config.Name("align_x")
        @Config.Comment("The length of which the map is offset in the X-axis")
        public double align_x = 0;

        @Config.Name("align_z")
        @Config.Comment("The length of which the map is offset in the Z-axis")
        public double align_z = 0;

        @Config.Name("y_axis")
        @Config.Comment("The Y-level where the map is rendered")
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

    }



    @Config.Name("render_settings")
    @Config.Comment("General render settings")
    public static final RenderSettings RENDER_SETTINGS = new RenderSettings();



    private static class ConfigDataCache {
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
    private static class ConfigEventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if(event.getModID().equals(BTETerraRenderer.MODID)) {
                BTRConfig.save();
            }
        }

    }

}
