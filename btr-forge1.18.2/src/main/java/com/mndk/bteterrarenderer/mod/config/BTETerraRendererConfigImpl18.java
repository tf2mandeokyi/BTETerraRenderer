package com.mndk.bteterrarenderer.mod.config;

import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import static com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig.*;

public class BTETerraRendererConfigImpl18 {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    public static boolean doRender;
    private static final ForgeConfigSpec.ConfigValue<Boolean> GENERAL_DO_RENDER;

    public static String mapServiceCategory, mapServiceId;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_CATEGORY;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_ID;

    public static final HologramConfigImpl HOLOGRAM_CONFIG = new HologramConfigImpl();
    public static class HologramConfigImpl {
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_X_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Z_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Boolean> RENDER_LOCK_NORTH;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_FLAT_MAP_Y_AXIS;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_OPACITY;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_DIFF_LIMIT;

        public double xAlign, yAlign, zAlign, opacity;
        public double flatMapYAxis, yDiffLimit;
        public boolean lockNorth;

        private static void register() {
            BUILDER.comment(HOLOGRAM_CONFIG_COMMENT).push(HOLOGRAM_CONFIG_NAME);
            RENDER_X_ALIGN = BUILDER.comment(HologramConfig.X_ALIGN_COMMENT)
                    .define(HologramConfig.X_ALIGN_NAME, HologramConfig.X_ALIGN_DEFAULT);
            RENDER_Y_ALIGN = BUILDER.comment(HologramConfig.Y_ALIGN_COMMENT)
                    .define(HologramConfig.Y_ALIGN_NAME, HologramConfig.Y_ALIGN_DEFAULT);
            RENDER_Z_ALIGN = BUILDER.comment(HologramConfig.Z_ALIGN_COMMENT)
                    .define(HologramConfig.Z_ALIGN_NAME, HologramConfig.Z_ALIGN_DEFAULT);
            RENDER_LOCK_NORTH = BUILDER.comment(HologramConfig.LOCK_NORTH_COMMENT)
                    .define(HologramConfig.LOCK_NORTH_NAME, HologramConfig.LOCK_NORTH_DEFAULT);
            RENDER_FLAT_MAP_Y_AXIS = BUILDER.comment(HologramConfig.FLAT_MAP_Y_AXIS_COMMENT)
                    .define(HologramConfig.FLAT_MAP_Y_AXIS_NAME, HologramConfig.FLAT_MAP_Y_AXIS_DEFAULT);
            RENDER_OPACITY = BUILDER.comment(HologramConfig.OPACITY_COMMENT)
                    .defineInRange(HologramConfig.OPACITY_NAME, HologramConfig.OPACITY_DEFAULT, HologramConfig.OPACITY_MIN, HologramConfig.OPACITY_MAX);
            RENDER_Y_DIFF_LIMIT = BUILDER.comment(HologramConfig.Y_DIFF_LIMIT_COMMENT)
                    .define(HologramConfig.Y_DIFF_LIMIT_NAME, HologramConfig.Y_DIFF_LIMIT_DEFAULT);
            BUILDER.pop();
        }

        private static void saveConfig() {
            RENDER_X_ALIGN.set(HOLOGRAM_CONFIG.xAlign);
            RENDER_Y_ALIGN.set(HOLOGRAM_CONFIG.yAlign);
            RENDER_Z_ALIGN.set(HOLOGRAM_CONFIG.zAlign);
            RENDER_LOCK_NORTH.set(HOLOGRAM_CONFIG.lockNorth);
            RENDER_FLAT_MAP_Y_AXIS.set(HOLOGRAM_CONFIG.flatMapYAxis);
            RENDER_OPACITY.set(HOLOGRAM_CONFIG.opacity);
            RENDER_Y_DIFF_LIMIT.set(HOLOGRAM_CONFIG.yDiffLimit);
        }

        private static void readConfig() {
            HOLOGRAM_CONFIG.xAlign = RENDER_X_ALIGN.get();
            HOLOGRAM_CONFIG.yAlign = RENDER_Y_ALIGN.get();
            HOLOGRAM_CONFIG.zAlign = RENDER_Z_ALIGN.get();
            HOLOGRAM_CONFIG.lockNorth = RENDER_LOCK_NORTH.get();
            HOLOGRAM_CONFIG.flatMapYAxis = RENDER_FLAT_MAP_Y_AXIS.get();
            HOLOGRAM_CONFIG.opacity = RENDER_OPACITY.get();
            HOLOGRAM_CONFIG.yDiffLimit = RENDER_Y_DIFF_LIMIT.get();
        }
    }

    public static final UIConfigImpl UI_CONFIG = new UIConfigImpl();
    public static class UIConfigImpl {
        private static ForgeConfigSpec.ConfigValue<SidebarSide> UI_SIDEBAR_SIDE;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_WIDTH;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_OPACITY;

        public SidebarSide sidebarSide;
        public double sidebarWidth;
        public double sidebarOpacity;

        private static void register() {
            BUILDER.comment(UI_CONFIG_COMMENT).push(UI_CONFIG_NAME);
            UI_SIDEBAR_SIDE = BUILDER.comment(UIConfig.SIDEBAR_SIDE_COMMENT)
                    .defineEnum(UIConfig.SIDEBAR_SIDE_NAME, UIConfig.SIDEBAR_SIDE_DEFAULT);
            UI_SIDEBAR_WIDTH = BUILDER.comment(UIConfig.SIDEBAR_WIDTH_COMMENT)
                    .defineInRange(UIConfig.SIDEBAR_WIDTH_NAME, UIConfig.SIDEBAR_WIDTH_DEFAULT, UIConfig.SIDEBAR_WIDTH_MIN, UIConfig.SIDEBAR_WIDTH_MAX);
            UI_SIDEBAR_OPACITY = BUILDER.comment(UIConfig.SIDEBAR_OPACITY_COMMENT)
                    .defineInRange(UIConfig.SIDEBAR_OPACITY_NAME, UIConfig.SIDEBAR_OPACITY_DEFAULT, UIConfig.SIDEBAR_OPACITY_MIN, UIConfig.SIDEBAR_OPACITY_MAX);
            BUILDER.pop();
        }

        private static void saveConfig() {
            UI_SIDEBAR_SIDE.set(UI_CONFIG.sidebarSide);
            UI_SIDEBAR_WIDTH.set(UI_CONFIG.sidebarWidth);
            UI_SIDEBAR_OPACITY.set(UI_CONFIG.sidebarOpacity);
        }

        private static void readConfig() {
            UI_CONFIG.sidebarSide = UI_SIDEBAR_SIDE.get();
            UI_CONFIG.sidebarWidth = UI_SIDEBAR_WIDTH.get();
            UI_CONFIG.sidebarOpacity = UI_SIDEBAR_OPACITY.get();
        }
    }

    public static void saveConfig() {
        GENERAL_DO_RENDER.set(doRender);
        GENERAL_MAP_SERVICE_CATEGORY.set(mapServiceCategory);
        GENERAL_MAP_SERVICE_ID.set(mapServiceId);
        HologramConfigImpl.saveConfig();
        UIConfigImpl.saveConfig();
    }

    public static void readConfig() {
        doRender = GENERAL_DO_RENDER.get();
        mapServiceCategory = GENERAL_MAP_SERVICE_CATEGORY.get();
        mapServiceId = GENERAL_MAP_SERVICE_ID.get();
        HologramConfigImpl.readConfig();
        UIConfigImpl.readConfig();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);
    }

    static {
        BUILDER.push("General Settings");
        GENERAL_DO_RENDER = BUILDER.comment(DO_RENDER_COMMENT)
                .define(DO_RENDER_NAME, DO_RENDER_DEFAULT);
        GENERAL_MAP_SERVICE_CATEGORY = BUILDER.comment(MAP_SERVICE_CATEGORY_COMMENT)
                .define(MAP_SERVICE_CATEGORY_NAME, MAP_SERVICE_CATEGORY_DEFAULT);
        GENERAL_MAP_SERVICE_ID = BUILDER.comment(MAP_SERVICE_ID_COMMENT)
                .define(MAP_SERVICE_ID_NAME, MAP_SERVICE_ID_DEFAULT);
        BUILDER.pop();
        HologramConfigImpl.register();
        UIConfigImpl.register();
        CONFIG_SPEC = BUILDER.build();
    }
}