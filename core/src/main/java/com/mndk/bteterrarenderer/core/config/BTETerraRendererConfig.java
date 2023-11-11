package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.TileMapService;

@SuppressWarnings("unused")
public class BTETerraRendererConfig {
    public static final BTETerraRendererConfig INSTANCE = new BTETerraRendererConfig();

    public static final String DO_RENDER_NAME = "Do Render";
    public static final String DO_RENDER_COMMENT = "Maps will be rendered if enabled";
    /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
    public static boolean doRender = false;
    public boolean isDoRender() { return doRender; }
    public void setDoRender(boolean doRender) { BTETerraRendererConfig.doRender = doRender; }

    public static final String MAP_SERVICE_CATEGORY_NAME = "Map Service Category";
    public static final String MAP_SERVICE_CATEGORY_COMMENT = "";
    /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
    public static String mapServiceCategory = "Global";
    public String getMapServiceCategory() { return mapServiceCategory; }
    public void setMapServiceCategory(String mapServiceCategory) { BTETerraRendererConfig.mapServiceCategory = mapServiceCategory; }

    public static final String MAP_SERVICE_ID_NAME = "Map Service ID";
    public static final String MAP_SERVICE_ID_COMMENT = "";
    /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
    public static String mapServiceId = "osm";
    public String getMapServiceId() { return mapServiceId; }
    public void setMapServiceId(String mapServiceId) { BTETerraRendererConfig.mapServiceId = mapServiceId; }

    public static final String HOLOGRAM_CONFIG_NAME = "Hologram Settings";
    public static final String HOLOGRAM_CONFIG_COMMENT = "Hologram render settings";
    public static class HologramConfig {
        public static final HologramConfig INSTANCE = new HologramConfig();

        public static final String X_ALIGN_NAME = "X Align";
        public static final String X_ALIGN_COMMENT = "The amount of which the map is offset on the X-axis";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double xAlign = 0.0;
        public double getXAlign() { return xAlign; }
        public void setXAlign(double xAlign) { HologramConfig.xAlign = xAlign; }

        public static final String Y_ALIGN_NAME = "Y Align";
        public static final String Y_ALIGN_COMMENT = "The amount of which the map is offset on the Y-axis.\nThis is only used for 3d maps.";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double yAlign = 0.0;
        public double getYAlign() { return yAlign; }
        public void setYAlign(double yAlign) { HologramConfig.yAlign = yAlign; }

        public static final String Z_ALIGN_NAME = "Z Align";
        public static final String Z_ALIGN_COMMENT = "The amount of which the map is offset on the Z-axis.";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double zAlign = 0.0;
        public double getZAlign() { return zAlign; }
        public void setZAlign(double zAlign) { HologramConfig.zAlign = zAlign; }

        public static final String LOCK_NORTH_NAME = "Lock North";
        public static final String LOCK_NORTH_COMMENT = "The map aligner direction will be locked to north if this is enabled.";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static boolean lockNorth = false;
        public boolean isLockNorth() { return lockNorth; }
        public void setLockNorth(boolean lockNorth) { HologramConfig.lockNorth = lockNorth; }

        public static final String FLAT_MAP_Y_AXIS_NAME = "Flat Map Y Axis";
        public static final String FLAT_MAP_Y_AXIS_COMMENT = "The in-game Y-coordinate value at which the flat map is rendered.";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double flatMapYAxis = 4;
        public double getFlatMapYAxis() { return flatMapYAxis; }
        public void setFlatMapYAxis(double flatMapYAxis) { HologramConfig.flatMapYAxis = flatMapYAxis; }

        public static final String OPACITY_NAME = "Opacity";
        public static final String OPACITY_COMMENT = "The map opacity";
        public static final double OPACITY_MIN = 0;
        public static final double OPACITY_MAX = 1;
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double opacity = 0.7;
        public double getOpacity() { return opacity; }
        public void setOpacity(double opacity) { HologramConfig.opacity = opacity; }

        public static final String Y_DIFF_LIMIT_NAME = "Y Diff Limit";
        public static final String Y_DIFF_LIMIT_COMMENT = "Puts limit on how far the map is from the player to be rendered.";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double yDiffLimit = 1000;
        public double getYDiffLimit() { return yDiffLimit; }
        public void setYDiffLimit(double yDiffLimit) { HologramConfig.yDiffLimit = yDiffLimit; }
    }

    public static final String UI_CONFIG_NAME = "UI Settings";
    public static final String UI_CONFIG_COMMENT = "General UI settings";
    public static class UIConfig {
        public static final UIConfig INSTANCE = new UIConfig();

        public static final String SIDEBAR_SIDE_NAME = "Sidebar Side";
        public static final String SIDEBAR_SIDE_COMMENT = "";
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static SidebarSide sidebarSide = SidebarSide.RIGHT;
        public SidebarSide getSidebarSide() { return sidebarSide; }
        public void setSidebarSide(SidebarSide side) { UIConfig.sidebarSide = side; }

        public static final String SIDEBAR_WIDTH_NAME = "Sidebar Width";
        public static final String SIDEBAR_WIDTH_COMMENT = "";
        public static final double SIDEBAR_WIDTH_MIN = 130;
        public static final double SIDEBAR_WIDTH_MAX = 270;
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double sidebarWidth = 200;
        public double getSidebarWidth() { return sidebarWidth; }
        public void setSidebarWidth(double sidebarWidth) { UIConfig.sidebarWidth = sidebarWidth; }

        public static final String SIDEBAR_OPACITY_NAME = "Sidebar Opacity";
        public static final String SIDEBAR_OPACITY_COMMENT = "";
        public static final double SIDEBAR_OPACITY_MIN = 0;
        public static final double SIDEBAR_OPACITY_MAX = 1;
        /** Do not use this on the core project! This is only for modded projects. Use getter/setter instead */
        public static double sidebarOpacity = 0.7;
        public double getSidebarOpacity() { return sidebarOpacity; }
        public void setSidebarOpacity(double sidebarOpacity) { UIConfig.sidebarOpacity = sidebarOpacity; }
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
