package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileImageCacheManager;
import com.mndk.bteterrarenderer.tile.TileMapService;
import lombok.Getter;
import lombok.Setter;

@ConfigAnnotations
public abstract class BTETerraRendererConfig {


    @Getter @Setter
    @ConfigAnnotations.Ignore
    private boolean doRender = false;
    public boolean toggleRender() {
        doRender = !doRender;
        return doRender;
    }


    @Getter
    @ConfigAnnotations.Name("map_service_category")
    public String mapServiceCategory = "Global";

    @Getter
    @ConfigAnnotations.Name("map_service_id")
    public String mapServiceId = "osm";


    @ConfigAnnotations.Name("render_settings")
    @ConfigAnnotations.Comment("General render settings")
    public final RenderSettings renderSettings = new RenderSettings();
    @Getter @Setter
    public static final class RenderSettings {

        @ConfigAnnotations.Name("align_x")
        @ConfigAnnotations.Comment("The amount of which the map is offset along the X-axis")
        public double xAlign = 0;

        @ConfigAnnotations.Name("align_z")
        @ConfigAnnotations.Comment("The amount of which the map is offset along the Z-axis")
        public double zAlign = 0;

        @ConfigAnnotations.Name("lock_north")
        @ConfigAnnotations.Comment("Whether to lock the direction of the map aligner to north")
        public boolean lockNorth = false;

        @ConfigAnnotations.Name("y_axis")
        @ConfigAnnotations.Comment("The y-coordinate at which the map is rendered")
        public double yAxis = 4;

        @ConfigAnnotations.Name("opacity")
        @ConfigAnnotations.Comment("The opacity of the map")
        @ConfigAnnotations.RangeDouble(min = 0, max = 1)
        @ConfigAnnotations.SlidingOption
        public double opacity = 0.7;

        @ConfigAnnotations.Name("zoom")
        @ConfigAnnotations.Comment("The higher the value, the more the map's resolution will increase.")
        @ConfigAnnotations.RangeInt(min = -3, max = 3)
        @ConfigAnnotations.SlidingOption
        public int zoom = 0;
        public void setZoom(int newZoom) {
            this.zoom = newZoom;
            TileImageCacheManager.getInstance().deleteAllRenderQueues();
        }

        @ConfigAnnotations.Name("size")
        @ConfigAnnotations.RangeInt(min = 1, max = 40)
        public int radius = 3;

        @ConfigAnnotations.Name("y_diff_limit")
        @ConfigAnnotations.Comment({
                "Puts limit on how far the map will be rendered from the player.",
                "If the difference between the player's y coordinate and the map's y coordinate",
                "exceeds the given limit, then the map rendering will be automatically stopped."
        })
        public double yDiffLimit = 1000;
    }


    @ConfigAnnotations.Name("ui_settings")
    public final UISettings uiSettings = new UISettings();
    @Getter @Setter
    public static class UISettings {
        @ConfigAnnotations.Name("sidebar_side")
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @ConfigAnnotations.Name("sidebar_width")
        @ConfigAnnotations.RangeInt(min = 130)
        // It's not necessary for the sidebar to fill more than a half of the game screen
        public int sidebarWidth = 200;

        @ConfigAnnotations.Name("sidebar_opacity")
        @ConfigAnnotations.RangeDouble(min = 0, max = 1)
        @ConfigAnnotations.SlidingOption
        public double sidebarOpacity = 0.5;

    }

    private static TileMapService tileMapService = getCurrentTileMapService();
    public static TileMapService getTileMapService() {
        return tileMapService;
    }

    public static boolean isRelativeZoomAvailable(int relativeZoom) {
        return tileMapService != null && tileMapService.isRelativeZoomAvailable(relativeZoom);
    }

    public static void setTileMapService(String categoryName, String mapId) {
        tileMapService = TMSYamlLoader.INSTANCE.result.getItem(categoryName, mapId);

        BTETerraRendererConfig config = BTETerraRendererCore.CONFIG;
        config.mapServiceCategory = categoryName;
        config.mapServiceId = mapId;

        TileImageCacheManager.getInstance().deleteAllRenderQueues();
    }

    public static void refreshTileMapService() {
        tileMapService = getCurrentTileMapService();
    }

    private static TileMapService getCurrentTileMapService() {
        BTETerraRendererConfig config = BTETerraRendererCore.CONFIG;
        return TMSYamlLoader.INSTANCE.result.getItem(config.getMapServiceCategory(), config.getMapServiceId());
    }

    public abstract void save();

}
