package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class BTRConfigConnectorImpl18 implements BTRConfigConnector {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    public static String MAP_SERVICE_CATEGORY, MAP_SERVICE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_CATEGORY;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_ID;

    public String getMapServiceCategory() {
        return MAP_SERVICE_CATEGORY;
    }
    public void setMapServiceCategory(String mapServiceCategory) {
        MAP_SERVICE_CATEGORY = mapServiceCategory;
    }
    public String getMapServiceId() {
        return MAP_SERVICE_ID;
    }
    public void setMapServiceId(String mapServiceId) {
        MAP_SERVICE_ID = mapServiceId;
    }
    public HologramSettingsConnector getHologramSettings() {
        return RENDER;
    }
    public UISettingsConnector getUiSettings() {
        return UI;
    }

    private static final HologramSettingsConnector RENDER = new HologramSettingsConnectorImpl();
    @Getter @Setter
    private static class HologramSettingsConnectorImpl implements HologramSettingsConnector {
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_X_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Z_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Boolean> RENDER_LOCK_NORTH;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_FLAT_MAP_Y_AXIS; // TODO: Rename this
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_OPACITY;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_DIFF_LIMIT;

        public double xAlign, yAlign, zAlign, opacity;
        public double flatMapYAxis, yDiffLimit;
        public int radius, relativeZoomValue;
        public boolean lockNorth;

        private static void register() {
            BUILDER.comment(HOLOGRAM_SETTINGS_COMMENT).push(HOLOGRAM_SETTINGS_NAME);
            RENDER_X_ALIGN = BUILDER.comment(X_ALIGN_COMMENT)
                    .define(X_ALIGN_NAME, 0.0);
            RENDER_Y_ALIGN = BUILDER.comment(Y_ALIGN_COMMENT)
                    .define(Y_ALIGN_NAME, 0.0);
            RENDER_Z_ALIGN = BUILDER.comment(Z_ALIGN_COMMENT)
                    .define(Z_ALIGN_NAME, 0.0);
            RENDER_LOCK_NORTH = BUILDER.comment(LOCK_NORTH_COMMENT)
                    .define(LOCK_NORTH_NAME, false);
            RENDER_FLAT_MAP_Y_AXIS = BUILDER.comment(FLAT_MAP_Y_AXIS_COMMENT)
                    .define(FLAT_MAP_Y_AXIS_NAME, 100.0);
            RENDER_OPACITY = BUILDER.comment(OPACITY_COMMENT)
                    .defineInRange(OPACITY_NAME, 0.7, 0.0, 1.0);
            RENDER_Y_DIFF_LIMIT = BUILDER.comment(Y_DIFF_LIMIT_COMMENT)
                    .define(Y_DIFF_LIMIT_NAME, 1000.0);
            BUILDER.pop();
        }

        private static void saveConfig() {
            RENDER_X_ALIGN.set(RENDER.getXAlign());
            RENDER_Y_ALIGN.set(RENDER.getYAlign());
            RENDER_Z_ALIGN.set(RENDER.getZAlign());
            RENDER_LOCK_NORTH.set(RENDER.isLockNorth());
            RENDER_FLAT_MAP_Y_AXIS.set(RENDER.getFlatMapYAxis());
            RENDER_OPACITY.set(RENDER.getOpacity());
            RENDER_Y_DIFF_LIMIT.set(RENDER.getYDiffLimit());
        }

        private static void readConfig() {
            RENDER.setXAlign(RENDER_X_ALIGN.get());
            RENDER.setYAlign(RENDER_Y_ALIGN.get());
            RENDER.setZAlign(RENDER_Z_ALIGN.get());
            RENDER.setLockNorth(RENDER_LOCK_NORTH.get());
            RENDER.setFlatMapYAxis(RENDER_FLAT_MAP_Y_AXIS.get());
            RENDER.setOpacity(RENDER_OPACITY.get());
            RENDER.setYDiffLimit(RENDER_Y_DIFF_LIMIT.get());
        }
    }

    private static final UISettingsConnector UI = new UISettingsConnectorImpl();
    @Getter @Setter
    private static class UISettingsConnectorImpl implements UISettingsConnector {
        private static ForgeConfigSpec.ConfigValue<SidebarSide> UI_SIDEBAR_SIDE;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_WIDTH;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_OPACITY;

        public SidebarSide sidebarSide;
        public double sidebarWidth;
        public double sidebarOpacity;

        private static void register() {
            BUILDER.comment(UI_SETTINGS_COMMENT).push(UI_SETTINGS_NAME);
            UI_SIDEBAR_SIDE = BUILDER.comment(SIDEBAR_SIDE_COMMENT)
                    .defineEnum(SIDEBAR_SIDE_NAME, SidebarSide.RIGHT);
            UI_SIDEBAR_WIDTH = BUILDER.comment(SIDEBAR_WIDTH_COMMENT)
                    .defineInRange(SIDEBAR_WIDTH_NAME, 200.0, 130.0, 270.0);
            UI_SIDEBAR_OPACITY = BUILDER.comment(SIDEBAR_OPACITY_COMMENT)
                    .defineInRange(SIDEBAR_OPACITY_NAME, 0.5, 0.0, 1.0);
            BUILDER.pop();
        }

        private static void saveConfig() {
            UI_SIDEBAR_SIDE.set(UI.getSidebarSide());
            UI_SIDEBAR_WIDTH.set(UI.getSidebarWidth());
            UI_SIDEBAR_OPACITY.set(UI.getSidebarOpacity());
        }

        private static void readConfig() {
            UI.setSidebarSide(UI_SIDEBAR_SIDE.get());
            UI.setSidebarWidth(UI_SIDEBAR_WIDTH.get());
            UI.setSidebarOpacity(UI_SIDEBAR_OPACITY.get());
        }
    }

    public void saveConfig() {
        GENERAL_MAP_SERVICE_CATEGORY.set(MAP_SERVICE_CATEGORY);
        GENERAL_MAP_SERVICE_ID.set(MAP_SERVICE_ID);
        HologramSettingsConnectorImpl.saveConfig();
        UISettingsConnectorImpl.saveConfig();
    }

    public void readConfig() {
        MAP_SERVICE_CATEGORY = GENERAL_MAP_SERVICE_CATEGORY.get();
        MAP_SERVICE_ID = GENERAL_MAP_SERVICE_ID.get();
        HologramSettingsConnectorImpl.readConfig();
        UISettingsConnectorImpl.readConfig();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);
    }

    static {
        BUILDER.push("General Settings");
        GENERAL_MAP_SERVICE_CATEGORY = BUILDER.comment(MAP_SERVICE_CATEGORY_COMMENT)
                .define(MAP_SERVICE_CATEGORY_NAME, "Global");
        GENERAL_MAP_SERVICE_ID = BUILDER.comment(MAP_SERVICE_ID_COMMENT)
                .define(MAP_SERVICE_ID_NAME, "osm");
        BUILDER.pop();
        HologramSettingsConnectorImpl.register();
        UISettingsConnectorImpl.register();
        CONFIG_SPEC = BUILDER.build();
    }
}