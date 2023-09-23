package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.TileMapService;

// TODO: Generalize this. This looks terrible
@SuppressWarnings("unused")
public class BTETerraRendererConfig {
    public static final BTETerraRendererConfig INSTANCE = new BTETerraRendererConfig();

    public static final String DO_RENDER_NAME = "Do Render";
    public static final String DO_RENDER_COMMENT = "Maps will be rendered if enabled";
    public static final boolean DO_RENDER_DEFAULT = false;
    public boolean isDoRender() { return DO_RENDER_DEFAULT; }
    public void setDoRender(boolean doRender) {}

    public static final String MAP_SERVICE_CATEGORY_NAME = "Map Service Category";
    public static final String MAP_SERVICE_CATEGORY_COMMENT = "";
    public static final String MAP_SERVICE_CATEGORY_DEFAULT = "Global";
    public String getMapServiceCategory() { return MAP_SERVICE_CATEGORY_DEFAULT; }
    public void setMapServiceCategory(String mapServiceCategory) {}

    public static final String MAP_SERVICE_ID_NAME = "Map Service ID";
    public static final String MAP_SERVICE_ID_COMMENT = "";
    public static final String MAP_SERVICE_ID_DEFAULT = "osm";
    public String getMapServiceId() { return MAP_SERVICE_ID_DEFAULT; }
    public void setMapServiceId(String mapServiceId) {}

    public static final String HOLOGRAM_CONFIG_NAME = "Hologram Settings";
    public static final String HOLOGRAM_CONFIG_COMMENT = "Hologram render settings";
    public static class HologramConfig {
        public static final HologramConfig INSTANCE = new HologramConfig();

        public static final String X_ALIGN_NAME = "X Align";
        public static final String X_ALIGN_COMMENT = "The amount of which the map is offset on the X-axis";
        public static final double X_ALIGN_DEFAULT = 0.0;
        public double getXAlign() { return X_ALIGN_DEFAULT; }
        public void setXAlign(double xAlign) {}

        public static final String Y_ALIGN_NAME = "Y Align";
        public static final String Y_ALIGN_COMMENT = "The amount of which the map is offset on the Y-axis.\nThis is only used for 3d maps.";
        public static final double Y_ALIGN_DEFAULT = 0.0;
        public double getYAlign() { return Y_ALIGN_DEFAULT; }
        public void setYAlign(double yAlign) {}

        public static final String Z_ALIGN_NAME = "Z Align";
        public static final String Z_ALIGN_COMMENT = "The amount of which the map is offset on the Z-axis.";
        public static final double Z_ALIGN_DEFAULT = 0.0;
        public double getZAlign() { return Z_ALIGN_DEFAULT; }
        public void setZAlign(double zAlign) {}

        public static final String LOCK_NORTH_NAME = "Lock North";
        public static final String LOCK_NORTH_COMMENT = "The map aligner direction will be locked to north if this is enabled.";
        public static final boolean LOCK_NORTH_DEFAULT = false;
        public boolean isLockNorth() { return LOCK_NORTH_DEFAULT; }
        public void setLockNorth(boolean lockNorth) {}

        public static final String FLAT_MAP_Y_AXIS_NAME = "Flat Map Y Axis";
        public static final String FLAT_MAP_Y_AXIS_COMMENT = "The in-game Y-coordinate value at which the flat map is rendered.";
        public static final double FLAT_MAP_Y_AXIS_DEFAULT = 4;
        public double getFlatMapYAxis() { return FLAT_MAP_Y_AXIS_DEFAULT; }
        public void setFlatMapYAxis(double flatMapYAxis) {}

        public static final String OPACITY_NAME = "Opacity";
        public static final String OPACITY_COMMENT = "The map opacity";
        public static final double OPACITY_DEFAULT = 0.7;
        public static final double OPACITY_MIN = 0;
        public static final double OPACITY_MAX = 1;
        public double getOpacity() { return OPACITY_DEFAULT; }
        public void setOpacity(double opacity) {}

        public static final String Y_DIFF_LIMIT_NAME = "Y Diff Limit";
        public static final String Y_DIFF_LIMIT_COMMENT = "Puts limit on how far the map is from the player to be rendered.";
        public static final double Y_DIFF_LIMIT_DEFAULT = 1000;
        public double getYDiffLimit() { return Y_DIFF_LIMIT_DEFAULT; }
        public void setYDiffLimit(double yDiffLimit) {}
    }

    public static final String UI_CONFIG_NAME = "UI Settings";
    public static final String UI_CONFIG_COMMENT = "General UI settings";
    public static class UIConfig {
        public static final UIConfig INSTANCE = new UIConfig();

        public static final String SIDEBAR_SIDE_NAME = "Sidebar Side";
        public static final String SIDEBAR_SIDE_COMMENT = "";
        public static final SidebarSide SIDEBAR_SIDE_DEFAULT = SidebarSide.RIGHT;
        public SidebarSide getSidebarSide() { return SIDEBAR_SIDE_DEFAULT; }
        public void setSidebarSide(SidebarSide side) {}

        public static final String SIDEBAR_WIDTH_NAME = "Sidebar Width";
        public static final String SIDEBAR_WIDTH_COMMENT = "";
        public static final double SIDEBAR_WIDTH_DEFAULT = 200;
        public static final double SIDEBAR_WIDTH_MIN = 130;
        public static final double SIDEBAR_WIDTH_MAX = 270;
        public double getSidebarWidth() { return SIDEBAR_WIDTH_DEFAULT; }
        public void setSidebarWidth(double sidebarWidth) {}

        public static final String SIDEBAR_OPACITY_NAME = "Sidebar Opacity";
        public static final String SIDEBAR_OPACITY_COMMENT = "";
        public static final double SIDEBAR_OPACITY_DEFAULT = 0.7;
        public static final double SIDEBAR_OPACITY_MIN = 0;
        public static final double SIDEBAR_OPACITY_MAX = 1;
        public double getSidebarOpacity() { return SIDEBAR_OPACITY_DEFAULT; }
        public void setSidebarOpacity(double sidebarOpacity) {}
    }

    public void toggleRender() {
        setDoRender(!isDoRender());
    }

    public void saveConfiguration() {
        refreshCurrentTileMapService();
    }
    public void loadConfiguration() {
        refreshCurrentTileMapService();
    }

    public CategoryMap.Wrapper<TileMapService<?>> getTileMapServiceWrapper() {
        return Storage.TMS_ON_DISPLAY;
    }

    public void setTileMapService(CategoryMap.Wrapper<TileMapService<?>> wrapped) {
        Storage.TMS_ON_DISPLAY = wrapped;
        setMapServiceCategory(wrapped.getParentCategory().getName());
        setMapServiceId(wrapped.getId());
    }

    public void refreshCurrentTileMapService() {
        Storage.TMS_ON_DISPLAY = TileMapServiceYamlLoader.INSTANCE
                .getResult()
                .getItemWrapper(INSTANCE.getMapServiceCategory(), INSTANCE.getMapServiceId());
    }

    private static class Storage {
        private static CategoryMap.Wrapper<TileMapService<?>> TMS_ON_DISPLAY;
    }
}
