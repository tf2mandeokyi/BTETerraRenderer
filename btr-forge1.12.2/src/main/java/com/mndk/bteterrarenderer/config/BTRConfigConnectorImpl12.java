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

    // Do render
    @Config.Ignore
    private static boolean doRender = false;
    public boolean isDoRender() { return doRender; }
    public void setDoRender(boolean b) { doRender = b; }

    // Map service category
    @Config.Name("map_service_category")
    public static String mapServiceCategory = "Global";
    public String getMapServiceCategory() { return mapServiceCategory; }
    public void setMapServiceCategory(String s) { mapServiceCategory = s; }

    // Map service ID
    @Config.Name("map_service_id")
    public static String mapServiceId = "osm";
    public String getMapServiceId() { return mapServiceId; }
    public void setMapServiceId(String s) { mapServiceId = s; }

    // Render settings
    @Config.Name("render_settings")
    @Config.Comment("General render settings")
    public static final RenderSettingsConnectorImpl RENDER_SETTINGS = new RenderSettingsConnectorImpl();
    public RenderSettingsConnector getRenderSettings() { return RENDER_SETTINGS; }

    // Render settings class
    @Getter @Setter
    public static final class RenderSettingsConnectorImpl implements RenderSettingsConnector {

        @Config.Name("align_x")
        @Config.Comment("The amount of which the map is offset along the in-game X-axis")
        public double xAlign = 0;

        @Config.Name("align_z")
        @Config.Comment("The amount of which the map is offset along the in-game Z-axis")
        public double zAlign = 0;

        @Config.Name("lock_north")
        @Config.Comment("The map aligner direction will be locked to north if enabled")
        public boolean lockNorth = false;

        @Config.Name("y_axis")
        @Config.Comment("The in-game y-coordinate value at which the map will be rendered")
        public double yAxis = 4;

        @Config.Name("opacity")
        @Config.Comment("The map opacity")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double opacity = 0.7;

        @Config.Name("zoom")
        @Config.Comment("The amount of map's resolution to increase; 0 is default")
        @Config.RangeInt(min = -3, max = 3)
        @Config.SlidingOption
        public int relativeZoomValue = 0;

        @Config.Name("size")
        @Config.RangeInt(min = 1, max = 40)
        public int radius = 3;

        @Config.Name("y_diff_limit")
        @Config.Comment("Puts limit on how far along the Y-axis the map will be rendered from the player.")
        public double yDiffLimit = 1000;
    }

    // UI settings
    @Config.Name("ui_settings")
    @Config.Comment("General UI settings")
    public static final UISettingsConnectorImpl UI_SETTINGS = new UISettingsConnectorImpl();
    public UISettingsConnector getUiSettings() { return UI_SETTINGS; }

    // UI settings class
    @Getter @Setter
    public static class UISettingsConnectorImpl implements UISettingsConnector {
        @Config.Name("sidebar_side")
        public SidebarSide sidebarSide = SidebarSide.RIGHT;

        @Config.Name("sidebar_width")
        @Config.RangeDouble(min = 130) // It's not necessary for the sidebar to fill more than a half of the game screen
        public double sidebarWidth = 200;

        @Config.Name("sidebar_opacity")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.SlidingOption
        public double sidebarOpacity = 0.5;
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
