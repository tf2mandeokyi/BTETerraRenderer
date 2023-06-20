package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = BTETerraRendererConstants.MODID)
@ConnectorImpl
public class BTRConfigConnectorImpl12 implements BTRConfigConnector {

    // Map service category
    @Config.Name(MAP_SERVICE_CATEGORY_NAME)
    @Config.Comment(MAP_SERVICE_CATEGORY_COMMENT)
    public static String mapServiceCategory = "Global";
    public String getMapServiceCategory() { return mapServiceCategory; }
    public void setMapServiceCategory(String s) { mapServiceCategory = s; }

    // Map service ID
    @Config.Name(MAP_SERVICE_ID_NAME)
    @Config.Comment(MAP_SERVICE_ID_COMMENT)
    public static String mapServiceId = "osm";
    public String getMapServiceId() { return mapServiceId; }
    public void setMapServiceId(String s) { mapServiceId = s; }

    // Render settings
    @Config.Name(HOLOGRAM_SETTINGS_NAME)
    @Config.Comment(HOLOGRAM_SETTINGS_COMMENT)
    public static final HologramSettingsConnectorImpl RENDER_SETTINGS = new HologramSettingsConnectorImpl();
    public HologramSettingsConnector getHologramSettings() { return RENDER_SETTINGS; }

    // Render settings class
    @Getter @Setter
    public static final class HologramSettingsConnectorImpl implements HologramSettingsConnector {

        @Config.Name(X_ALIGN_NAME)
        @Config.Comment(X_ALIGN_COMMENT)
        public double xAlign = 0;

        @Config.Name(Y_ALIGN_NAME)
        @Config.Comment(Y_ALIGN_COMMENT)
        public double yAlign = 0;

        @Config.Name(Z_ALIGN_NAME)
        @Config.Comment(Z_ALIGN_COMMENT)
        public double zAlign = 0;

        @Config.Name(LOCK_NORTH_NAME)
        @Config.Comment(LOCK_NORTH_COMMENT)
        public boolean lockNorth = false;

        @Config.Name(FLAT_MAP_Y_AXIS_NAME)
        @Config.Comment(FLAT_MAP_Y_AXIS_COMMENT)
        public double flatMapYAxis = 4;

        @Config.Name(OPACITY_NAME)
        @Config.Comment(OPACITY_COMMENT)
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double opacity = 0.7;

        @Config.Name(Y_DIFF_LIMIT_NAME)
        @Config.Comment(Y_DIFF_LIMIT_COMMENT)
        public double yDiffLimit = 1000;
    }

    // UI settings
    @Config.Name(UI_SETTINGS_NAME)
    @Config.Comment(UI_SETTINGS_COMMENT)
    public static final UISettingsConnectorImpl UI_SETTINGS = new UISettingsConnectorImpl();
    public UISettingsConnector getUiSettings() { return UI_SETTINGS; }

    // UI settings class
    @Getter @Setter
    public static class UISettingsConnectorImpl implements UISettingsConnector {
        @Config.Name(SIDEBAR_SIDE_NAME)
        @Config.Comment(SIDEBAR_SIDE_COMMENT)
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @Config.Name(SIDEBAR_WIDTH_NAME)
        @Config.Comment(SIDEBAR_WIDTH_COMMENT)
        @Config.RangeDouble(min = 130, max = 270)
        public double sidebarWidth = 200;

        @Config.Name(SIDEBAR_OPACITY_NAME)
        @Config.Comment(SIDEBAR_OPACITY_COMMENT)
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double sidebarOpacity = 0.7;
    }

    public void saveConfig() {
        ConfigManager.sync(BTETerraRendererConstants.MODID, Config.Type.INSTANCE);
    }
    public void readConfig() {}

    @Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID)
    public static class ConfigEventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (!event.getModID().equals(BTETerraRendererConstants.MODID)) return;
            BTRConfigConnector.save();
        }
    }
}
