package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.graphics.GraphicsModelManager;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.loader.CategoryMap;
import com.mndk.bteterrarenderer.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;

public interface BTRConfigConnector {
    BTRConfigConnector INSTANCE = ImplFinder.search();

    /**
     * Call {@link #save()} instead of this method
     */
    void saveConfig();
    void readConfig();

    default boolean isDoRender() {
        return Storage.RENDER;
    }
    default void setDoRender(boolean doRender) {
        Storage.RENDER = doRender;
    }

    String MAP_SERVICE_CATEGORY_NAME = "Map Service Category";
    String MAP_SERVICE_CATEGORY_COMMENT = "";
    String getMapServiceCategory();
    void setMapServiceCategory(String mapServiceCategory);

    String MAP_SERVICE_ID_NAME = "Map Service ID";
    String MAP_SERVICE_ID_COMMENT = "";
    String getMapServiceId();
    void setMapServiceId(String mapServiceId);

    String HOLOGRAM_SETTINGS_NAME = "Hologram Settings";
    String HOLOGRAM_SETTINGS_COMMENT = "Hologram render settings";
    HologramSettingsConnector getHologramSettings();
    interface HologramSettingsConnector {
        String X_ALIGN_NAME = "X Align";
        String X_ALIGN_COMMENT = "The amount of which the map is offset on the X-axis";
        double getXAlign();
        void setXAlign(double xAlign);

        String Y_ALIGN_NAME = "Y Align";
        String Y_ALIGN_COMMENT = "The amount of which the map is offset on the Y-axis.\nThis is only used for 3d maps.";
        double getYAlign();
        void setYAlign(double yAlign);

        String Z_ALIGN_NAME = "Z Align";
        String Z_ALIGN_COMMENT = "The amount of which the map is offset on the Z-axis.";
        double getZAlign();
        void setZAlign(double zAlign);

        String LOCK_NORTH_NAME = "Lock North";
        String LOCK_NORTH_COMMENT = "The map aligner direction will be locked to north if this is enabled.";
        boolean isLockNorth();
        void setLockNorth(boolean lockNorth);

        String FLAT_MAP_Y_AXIS_NAME = "Flat Map Y Axis";
        String FLAT_MAP_Y_AXIS_COMMENT = "The in-game Y-coordinate value at which the flat map is rendered.";
        double getFlatMapYAxis();
        void setFlatMapYAxis(double yAxis);

        String OPACITY_NAME = "Opacity";
        String OPACITY_COMMENT = "The map opacity";
        double getOpacity();
        void setOpacity(double opacity);

        String Y_DIFF_LIMIT_NAME = "Y Diff Limit";
        String Y_DIFF_LIMIT_COMMENT = "Puts limit on how far the map is from the player to be rendered.";
        double getYDiffLimit();
        void setYDiffLimit(double yDiffLimit);
    }

    String UI_SETTINGS_NAME = "UI Settings";
    String UI_SETTINGS_COMMENT = "General UI settings";
    UISettingsConnector getUiSettings();
    interface UISettingsConnector {
        String SIDEBAR_SIDE_NAME = "Sidebar Side";
        String SIDEBAR_SIDE_COMMENT = "";
        SidebarSide getSidebarSide();
        void setSidebarSide(SidebarSide side);

        String SIDEBAR_WIDTH_NAME = "Sidebar Width";
        String SIDEBAR_WIDTH_COMMENT = "";
        double getSidebarWidth();
        void setSidebarWidth(double sidebarWidth);

        String SIDEBAR_OPACITY_NAME = "Sidebar Opacity";
        String SIDEBAR_OPACITY_COMMENT = "";
        double getSidebarOpacity();
        void setSidebarOpacity(double sidebarOpacity);
    }

    default void toggleRender() {
        Storage.RENDER = !Storage.RENDER;
    }

    static void save() {
        INSTANCE.saveConfig();
        refreshTileMapService();
    }

    static void load() {
        INSTANCE.readConfig();
        refreshTileMapService();
    }

    static CategoryMap.Wrapper<TileMapService> getTileMapService() {
        return Storage.TMS_ON_DISPLAY;
    }

    static void setTileMapService(CategoryMap.Wrapper<TileMapService> wrapped) {
        Storage.TMS_ON_DISPLAY = wrapped;
        INSTANCE.setMapServiceCategory(wrapped.getParentCategory().getName());
        INSTANCE.setMapServiceId(wrapped.getId());

        GraphicsModelManager.INSTANCE.newQueue();
    }

    static void refreshTileMapService() {
        ConfigLoaders.loadAll();
        Storage.TMS_ON_DISPLAY = TMSYamlLoader.INSTANCE.result.getItemWrapper(INSTANCE.getMapServiceCategory(), INSTANCE.getMapServiceId());
    }

    class Storage {
        private static CategoryMap.Wrapper<TileMapService> TMS_ON_DISPLAY;
        private static boolean RENDER = false;
    }
}
