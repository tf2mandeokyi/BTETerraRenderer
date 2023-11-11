package com.mndk.bteterrarenderer.mod.config;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import static com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig.*;

public class BTETerraRendererConfigImpl {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    private static final ForgeConfigSpec.ConfigValue<Boolean> GENERAL_DO_RENDER;
    public static void saveRenderState() {
        GENERAL_DO_RENDER.set(BTETerraRendererConfig.doRender);
    }

    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_CATEGORY;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_ID;

    public static class HologramConfigImpl {
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_X_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Z_ALIGN;
        private static ForgeConfigSpec.ConfigValue<Boolean> RENDER_LOCK_NORTH;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_FLAT_MAP_Y_AXIS;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_OPACITY;
        private static ForgeConfigSpec.ConfigValue<Double> RENDER_Y_DIFF_LIMIT;

        private static void register() {
            BUILDER.comment(HOLOGRAM_CONFIG_COMMENT).push(HOLOGRAM_CONFIG_NAME);
            RENDER_X_ALIGN = BUILDER.comment(HologramConfig.X_ALIGN_COMMENT)
                    .define(HologramConfig.X_ALIGN_NAME, HologramConfig.xAlign);
            RENDER_Y_ALIGN = BUILDER.comment(HologramConfig.Y_ALIGN_COMMENT)
                    .define(HologramConfig.Y_ALIGN_NAME, HologramConfig.yAlign);
            RENDER_Z_ALIGN = BUILDER.comment(HologramConfig.Z_ALIGN_COMMENT)
                    .define(HologramConfig.Z_ALIGN_NAME, HologramConfig.zAlign);
            RENDER_LOCK_NORTH = BUILDER.comment(HologramConfig.LOCK_NORTH_COMMENT)
                    .define(HologramConfig.LOCK_NORTH_NAME, HologramConfig.lockNorth);
            RENDER_FLAT_MAP_Y_AXIS = BUILDER.comment(HologramConfig.FLAT_MAP_Y_AXIS_COMMENT)
                    .define(HologramConfig.FLAT_MAP_Y_AXIS_NAME, HologramConfig.flatMapYAxis);
            RENDER_OPACITY = BUILDER.comment(HologramConfig.OPACITY_COMMENT)
                    .defineInRange(HologramConfig.OPACITY_NAME, HologramConfig.opacity, HologramConfig.OPACITY_MIN, HologramConfig.OPACITY_MAX);
            RENDER_Y_DIFF_LIMIT = BUILDER.comment(HologramConfig.Y_DIFF_LIMIT_COMMENT)
                    .define(HologramConfig.Y_DIFF_LIMIT_NAME, HologramConfig.yDiffLimit);
            BUILDER.pop();
        }

        private static void saveConfig() {
            RENDER_X_ALIGN.set(HologramConfig.xAlign);
            RENDER_Y_ALIGN.set(HologramConfig.yAlign);
            RENDER_Z_ALIGN.set(HologramConfig.zAlign);
            RENDER_LOCK_NORTH.set(HologramConfig.lockNorth);
            RENDER_FLAT_MAP_Y_AXIS.set(HologramConfig.flatMapYAxis);
            RENDER_OPACITY.set(HologramConfig.opacity);
            RENDER_Y_DIFF_LIMIT.set(HologramConfig.yDiffLimit);
        }

        private static void readConfig() {
            HologramConfig.xAlign = RENDER_X_ALIGN.get();
            HologramConfig.yAlign = RENDER_Y_ALIGN.get();
            HologramConfig.zAlign = RENDER_Z_ALIGN.get();
            HologramConfig.lockNorth = RENDER_LOCK_NORTH.get();
            HologramConfig.flatMapYAxis = RENDER_FLAT_MAP_Y_AXIS.get();
            HologramConfig.opacity = RENDER_OPACITY.get();
            HologramConfig.yDiffLimit = RENDER_Y_DIFF_LIMIT.get();
        }
    }

    public static class UIConfigImpl {
        private static ForgeConfigSpec.ConfigValue<SidebarSide> UI_SIDEBAR_SIDE;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_WIDTH;
        private static ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_OPACITY;

        private static void register() {
            BUILDER.comment(UI_CONFIG_COMMENT).push(UI_CONFIG_NAME);
            UI_SIDEBAR_SIDE = BUILDER.comment(UIConfig.SIDEBAR_SIDE_COMMENT)
                    .defineEnum(UIConfig.SIDEBAR_SIDE_NAME, UIConfig.sidebarSide);
            UI_SIDEBAR_WIDTH = BUILDER.comment(UIConfig.SIDEBAR_WIDTH_COMMENT)
                    .defineInRange(UIConfig.SIDEBAR_WIDTH_NAME, UIConfig.sidebarWidth, UIConfig.SIDEBAR_WIDTH_MIN, UIConfig.SIDEBAR_WIDTH_MAX);
            UI_SIDEBAR_OPACITY = BUILDER.comment(UIConfig.SIDEBAR_OPACITY_COMMENT)
                    .defineInRange(UIConfig.SIDEBAR_OPACITY_NAME, UIConfig.sidebarOpacity, UIConfig.SIDEBAR_OPACITY_MIN, UIConfig.SIDEBAR_OPACITY_MAX);
            BUILDER.pop();
        }

        private static void saveConfig() {
            UI_SIDEBAR_SIDE.set(UIConfig.sidebarSide);
            UI_SIDEBAR_WIDTH.set(UIConfig.sidebarWidth);
            UI_SIDEBAR_OPACITY.set(UIConfig.sidebarOpacity);
        }

        private static void readConfig() {
            UIConfig.sidebarSide = UI_SIDEBAR_SIDE.get();
            UIConfig.sidebarWidth = UI_SIDEBAR_WIDTH.get();
            UIConfig.sidebarOpacity = UI_SIDEBAR_OPACITY.get();
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
                .define(DO_RENDER_NAME, BTETerraRendererConfig.doRender);
        GENERAL_MAP_SERVICE_CATEGORY = BUILDER.comment(MAP_SERVICE_CATEGORY_COMMENT)
                .define(MAP_SERVICE_CATEGORY_NAME, BTETerraRendererConfig.mapServiceCategory);
        GENERAL_MAP_SERVICE_ID = BUILDER.comment(MAP_SERVICE_ID_COMMENT)
                .define(MAP_SERVICE_ID_NAME, BTETerraRendererConfig.mapServiceId);
        BUILDER.pop();
        HologramConfigImpl.register();
        UIConfigImpl.register();
        CONFIG_SPEC = BUILDER.build();
    }
}