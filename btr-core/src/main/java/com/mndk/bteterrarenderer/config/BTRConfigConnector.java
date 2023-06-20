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
    boolean isDoRender();               void setDoRender(boolean doRender);
    String getMapServiceCategory();     void setMapServiceCategory(String mapServiceCategory);
    String getMapServiceId();           void setMapServiceId(String mapServiceId);

    RenderSettingsConnector getRenderSettings();
    interface RenderSettingsConnector {
        double getXAlign();             void setXAlign(double xAlign);
        double getYAlign();             void setYAlign(double yAlign);
        double getZAlign();             void setZAlign(double zAlign);
        boolean isLockNorth();          void setLockNorth(boolean lockNorth);
        double getFlatMapYAxis();       void setFlatMapYAxis(double yAxis);
        double getOpacity();            void setOpacity(double opacity);
        int getRadius();                void setRadius(int radius);
        double getYDiffLimit();         void setYDiffLimit(double yDiffLimit);
        int getRelativeZoomValue();     void setRelativeZoomValue(int relativeZoomValue);

        default void setRelativeZoom(int newZoom) {
            this.setRelativeZoomValue(newZoom);
            GraphicsModelManager.INSTANCE.clearTextureRenderQueue();
        }
    }

    UISettingsConnector getUiSettings();
    interface UISettingsConnector {
        SidebarSide getSidebarSide();   void setSidebarSide(SidebarSide side);
        double getSidebarWidth();       void setSidebarWidth(double sidebarWidth);
        double getSidebarOpacity();     void setSidebarOpacity(double sidebarOpacity);
    }

    default void toggleRender() {
        setDoRender(!isDoRender());
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

        GraphicsModelManager.INSTANCE.clearTextureRenderQueue();
    }

    static void refreshTileMapService() {
        ConfigLoaders.loadAll();
        Storage.TMS_ON_DISPLAY = TMSYamlLoader.INSTANCE.result.getItemWrapper(INSTANCE.getMapServiceCategory(), INSTANCE.getMapServiceId());
    }

    class Storage {
        public static CategoryMap.Wrapper<TileMapService> TMS_ON_DISPLAY;
    }
}
