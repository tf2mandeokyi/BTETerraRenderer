package com.mndk.bteterrarenderer.mod.config;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig.*;

@Config(modid = BTETerraRendererConstants.MODID)
public class BTETerraRendererConfigImpl {

    @Config.Name(DO_RENDER_NAME)
    @Config.Comment(DO_RENDER_COMMENT)
    public static boolean doRender = BTETerraRendererConfig.doRender;

    @Config.Name(MAP_SERVICE_CATEGORY_NAME)
    @Config.Comment(MAP_SERVICE_CATEGORY_COMMENT)
    public static String mapServiceCategory = BTETerraRendererConfig.mapServiceCategory;

    @Config.Name(MAP_SERVICE_ID_NAME)
    @Config.Comment(MAP_SERVICE_ID_COMMENT)
    public static String mapServiceId = BTETerraRendererConfig.mapServiceId;

    @Config.Name(HOLOGRAM_CONFIG_NAME)
    @Config.Comment(HOLOGRAM_CONFIG_COMMENT)
    public static final HologramConfigImpl HOLOGRAM_CONFIG = new HologramConfigImpl();
    public static class HologramConfigImpl {
        @Config.Name(HologramConfig.X_ALIGN_NAME)
        @Config.Comment(HologramConfig.X_ALIGN_COMMENT)
        public double xAlign = HologramConfig.xAlign;

        @Config.Name(HologramConfig.Y_ALIGN_NAME)
        @Config.Comment(HologramConfig.Y_ALIGN_COMMENT)
        public double yAlign = HologramConfig.yAlign;

        @Config.Name(HologramConfig.Z_ALIGN_NAME)
        @Config.Comment(HologramConfig.Z_ALIGN_COMMENT)
        public double zAlign = HologramConfig.zAlign;

        @Config.Name(HologramConfig.LOCK_NORTH_NAME)
        @Config.Comment(HologramConfig.LOCK_NORTH_COMMENT)
        public boolean lockNorth = HologramConfig.lockNorth;

        @Config.Name(HologramConfig.FLAT_MAP_Y_AXIS_NAME)
        @Config.Comment(HologramConfig.FLAT_MAP_Y_AXIS_COMMENT)
        public double flatMapYAxis = HologramConfig.flatMapYAxis;

        @Config.Name(HologramConfig.OPACITY_NAME)
        @Config.Comment(HologramConfig.OPACITY_COMMENT)
        @Config.RangeDouble(min = HologramConfig.OPACITY_MIN, max = HologramConfig.OPACITY_MAX)
        @Config.SlidingOption
        public double opacity = HologramConfig.opacity;

        @Config.Name(HologramConfig.Y_DIFF_LIMIT_NAME)
        @Config.Comment(HologramConfig.Y_DIFF_LIMIT_COMMENT)
        public double yDiffLimit = HologramConfig.yDiffLimit;
    }

    @Config.Name(UI_CONFIG_NAME)
    @Config.Comment(UI_CONFIG_COMMENT)
    public static final UIConfigImpl UI_CONFIG = new UIConfigImpl();
    public static class UIConfigImpl {
        @Config.Name(UIConfig.SIDEBAR_SIDE_NAME)
        @Config.Comment(UIConfig.SIDEBAR_SIDE_COMMENT)
        public SidebarSide sidebarSide = UIConfig.sidebarSide;

        @Config.Name(UIConfig.SIDEBAR_WIDTH_NAME)
        @Config.Comment(UIConfig.SIDEBAR_WIDTH_COMMENT)
        @Config.RangeDouble(min = UIConfig.SIDEBAR_WIDTH_MIN, max = UIConfig.SIDEBAR_WIDTH_MAX)
        public double sidebarWidth = UIConfig.sidebarWidth;

        @Config.Name(UIConfig.SIDEBAR_OPACITY_NAME)
        @Config.Comment(UIConfig.SIDEBAR_OPACITY_COMMENT)
        @Config.RangeDouble(min = UIConfig.SIDEBAR_OPACITY_MIN, max = UIConfig.SIDEBAR_OPACITY_MAX)
        @Config.SlidingOption
        public double sidebarOpacity = UIConfig.sidebarOpacity;
    }

    public static void saveConfig() {
        ConfigManager.sync(BTETerraRendererConstants.MODID, Config.Type.INSTANCE);
    }

    @Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID)
    public static class ConfigEventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (!event.getModID().equals(BTETerraRendererConstants.MODID)) return;
            BTETerraRendererConfig.INSTANCE.saveConfiguration();
        }
    }
}
