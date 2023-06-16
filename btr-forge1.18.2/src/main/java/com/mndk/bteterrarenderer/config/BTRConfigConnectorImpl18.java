package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.ForgeConfigSpec;

public class BTRConfigConnectorImpl18 implements BTRConfigConnector {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    public static boolean DO_RENDER;
    private static final ForgeConfigSpec.ConfigValue<Boolean> GENERAL_DO_RENDER;

    public static String MAP_SERVICE_CATEGORY, MAP_SERVICE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_CATEGORY, GENERAL_MAP_SERVICE_ID;

    private static final RenderSettingsConnector RENDER = new RenderSettingsConnectorImpl();
    private static final ForgeConfigSpec.ConfigValue<Double> RENDER_X_ALIGN;
    private static final ForgeConfigSpec.ConfigValue<Double> RENDER_Z_ALIGN;
    private static final ForgeConfigSpec.ConfigValue<Boolean> RENDER_LOCK_NORTH;
    private static final ForgeConfigSpec.ConfigValue<Double> RENDER_Y_AXIS;
    private static final ForgeConfigSpec.ConfigValue<Double> RENDER_OPACITY;
    private static final ForgeConfigSpec.ConfigValue<Integer> RENDER_RADIUS;
    private static final ForgeConfigSpec.ConfigValue<Integer> RENDER_RELATIVE_ZOOM_VALUE;
    private static final ForgeConfigSpec.ConfigValue<Double> RENDER_Y_DIFF_LIMIT;

    private static final UISettingsConnector UI = new UISettingsConnectorImpl();
    private static final ForgeConfigSpec.ConfigValue<SidebarSide> UI_SIDEBAR_SIDE;
    private static final ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_WIDTH;
    private static final ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_OPACITY;

    public boolean isDoRender() {
        return DO_RENDER;
    }
    public void setDoRender(boolean doRender) {
        DO_RENDER = doRender;
    }
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
    public RenderSettingsConnector getRenderSettings() {
        return RENDER;
    }
    public UISettingsConnector getUiSettings() {
        return UI;
    }

    @Getter @Setter
    private static class RenderSettingsConnectorImpl implements RenderSettingsConnector {
        public double xAlign, zAlign, opacity;
        public double yAxis, yDiffLimit;
        public int radius, relativeZoomValue;
        public boolean lockNorth;
    }

    @Getter @Setter
    private static class UISettingsConnectorImpl implements UISettingsConnector {
        public SidebarSide sidebarSide;
        public double sidebarWidth;
        public double sidebarOpacity;
    }

    public void saveConfig() {
        GENERAL_DO_RENDER.set(DO_RENDER);
        GENERAL_MAP_SERVICE_CATEGORY.set(MAP_SERVICE_CATEGORY);
        GENERAL_MAP_SERVICE_ID.set(MAP_SERVICE_ID);

        RENDER_X_ALIGN.set(RENDER.getXAlign());
        RENDER_Z_ALIGN.set(RENDER.getZAlign());
        RENDER_LOCK_NORTH.set(RENDER.isLockNorth());
        RENDER_Y_AXIS.set(RENDER.getYAxis());
        RENDER_OPACITY.set(RENDER.getOpacity());
        RENDER_RADIUS.set(RENDER.getRadius());
        RENDER_RELATIVE_ZOOM_VALUE.set(RENDER.getRelativeZoomValue());
        RENDER_Y_DIFF_LIMIT.set(RENDER.getYDiffLimit());

        UI_SIDEBAR_SIDE.set(UI.getSidebarSide());
        UI_SIDEBAR_WIDTH.set(UI.getSidebarWidth());
        UI_SIDEBAR_OPACITY.set(UI.getSidebarOpacity());
    }

    public void readConfig() {
        DO_RENDER = GENERAL_DO_RENDER.get();
        MAP_SERVICE_CATEGORY = GENERAL_MAP_SERVICE_CATEGORY.get();
        MAP_SERVICE_ID = GENERAL_MAP_SERVICE_ID.get();

        RENDER.setXAlign(RENDER_X_ALIGN.get());
        RENDER.setZAlign(RENDER_Z_ALIGN.get());
        RENDER.setLockNorth(RENDER_LOCK_NORTH.get());
        RENDER.setYAxis(RENDER_Y_AXIS.get());
        RENDER.setOpacity(RENDER_OPACITY.get());
        RENDER.setRadius(RENDER_RADIUS.get());
        RENDER.setRelativeZoomValue(RENDER_RELATIVE_ZOOM_VALUE.get());
        RENDER.setYDiffLimit(RENDER_Y_DIFF_LIMIT.get());

        UI.setSidebarSide(UI_SIDEBAR_SIDE.get());
        UI.setSidebarWidth(UI_SIDEBAR_WIDTH.get());
        UI.setSidebarOpacity(UI_SIDEBAR_OPACITY.get());
    }

    static {
        // TODO: Add descriptions
        // TODO: Make names and default values constant

        BUILDER.push("General Settings");

        GENERAL_DO_RENDER = BUILDER.define("Do Render", true); // TODO: set this back to false
        GENERAL_MAP_SERVICE_CATEGORY = BUILDER.define("Map Service Category", "Global");
        GENERAL_MAP_SERVICE_ID = BUILDER.define("Map Service ID", "osm");

        BUILDER.pop();
        BUILDER.push("Hologram Settings");

        RENDER_X_ALIGN = BUILDER.define("X Alignment", 0.0);
        RENDER_Z_ALIGN = BUILDER.define("Z Alignment", 0.0);
        RENDER_LOCK_NORTH = BUILDER.define("Lock North", false);
        RENDER_Y_AXIS = BUILDER.define("Y Axis", 300.0); // TODO: set this back to 4.0
        RENDER_OPACITY = BUILDER.defineInRange("Opacity", 0.7, 0.0, 1.0);
        RENDER_RADIUS = BUILDER.defineInRange("Radius", 3, 1, 40);
        RENDER_RELATIVE_ZOOM_VALUE = BUILDER.defineInRange("Zoom Value", 0, -3, 3);
        RENDER_Y_DIFF_LIMIT = BUILDER.define("Max Y Diff Limit", 1000.0);

        BUILDER.pop();
        BUILDER.push("UI Settings");

        UI_SIDEBAR_SIDE = BUILDER.define("Sidebar Side", SidebarSide.RIGHT);
        UI_SIDEBAR_WIDTH = BUILDER.defineInRange("Sidebar Width", 200.0, 130.0, 270.0);
        UI_SIDEBAR_OPACITY = BUILDER.defineInRange("Sidebar Opacity", 0.5, 0.0, 1.0);

        BUILDER.pop();
        CONFIG_SPEC = BUILDER.build();
    }
}