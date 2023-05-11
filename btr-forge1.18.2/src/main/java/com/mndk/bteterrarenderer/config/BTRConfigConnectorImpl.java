package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import net.minecraftforge.common.ForgeConfigSpec;

@ConnectorImpl
public class BTRConfigConnectorImpl implements BTRConfigConnector {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<Boolean> GENERAL_DO_RENDER;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_CATEGORY;
    private static final ForgeConfigSpec.ConfigValue<String> GENERAL_MAP_SERVICE_ID;

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
    private static final ForgeConfigSpec.ConfigValue<Integer> UI_SIDEBAR_WIDTH;
    private static final ForgeConfigSpec.ConfigValue<Double> UI_SIDEBAR_OPACITY;

    public boolean isDoRender() {
        return GENERAL_DO_RENDER.get();
    }

    public void setDoRender(boolean doRender) {
        GENERAL_DO_RENDER.set(doRender);
    }

    public String getMapServiceCategory() {
        return GENERAL_MAP_SERVICE_CATEGORY.get();
    }

    public void setMapServiceCategory(String mapServiceCategory) {
        GENERAL_MAP_SERVICE_CATEGORY.set(mapServiceCategory);
    }

    public String getMapServiceId() {
        return GENERAL_MAP_SERVICE_ID.get();
    }

    public void setMapServiceId(String mapServiceId) {
        GENERAL_MAP_SERVICE_ID.set(mapServiceId);
    }

    public RenderSettingsConnector getRenderSettings() {
        return RENDER;
    }

    public UISettingsConnector getUiSettings() {
        return UI;
    }

    private static class RenderSettingsConnectorImpl implements RenderSettingsConnector {
        public double getXAlign() {
            return RENDER_X_ALIGN.get();
        }

        public void setXAlign(double xAlign) {
            RENDER_X_ALIGN.set(xAlign);
        }

        public double getZAlign() {
            return RENDER_Z_ALIGN.get();
        }

        public void setZAlign(double zAlign) {
            RENDER_Z_ALIGN.set(zAlign);
        }

        public boolean isLockNorth() {
            return RENDER_LOCK_NORTH.get();
        }

        public void setLockNorth(boolean lockNorth) {
            RENDER_LOCK_NORTH.set(lockNorth);
        }

        public double getYAxis() {
            return RENDER_Y_AXIS.get();
        }

        public void setYAxis(double yAxis) {
            RENDER_Y_AXIS.set(yAxis);
        }

        public double getOpacity() {
            return RENDER_OPACITY.get();
        }

        public void setOpacity(double opacity) {
            RENDER_OPACITY.set(opacity);
        }

        public int getRadius() {
            return RENDER_RADIUS.get();
        }

        public void setRadius(int radius) {
            RENDER_RADIUS.set(radius);
        }

        public double getYDiffLimit() {
            return RENDER_Y_DIFF_LIMIT.get();
        }

        public void setYDiffLimit(double yDiffLimit) {
            RENDER_Y_DIFF_LIMIT.set(yDiffLimit);
        }

        public int getRelativeZoomValue() {
            return RENDER_RELATIVE_ZOOM_VALUE.get();
        }

        public void setRelativeZoomValue(int relativeZoomValue) {
            RENDER_RELATIVE_ZOOM_VALUE.set(relativeZoomValue);
        }
    }

    private static class UISettingsConnectorImpl implements UISettingsConnector {

        @Override
        public SidebarSide getSidebarSide() {
            return UI_SIDEBAR_SIDE.get();
        }

        @Override
        public void setSidebarSide(SidebarSide side) {
            UI_SIDEBAR_SIDE.set(side);
        }

        @Override
        public int getSidebarWidth() {
            return UI_SIDEBAR_WIDTH.get();
        }

        @Override
        public void setSidebarWidth(int sidebarWidth) {
            UI_SIDEBAR_WIDTH.set(sidebarWidth);
        }

        @Override
        public double getSidebarOpacity() {
            return UI_SIDEBAR_OPACITY.get();
        }

        @Override
        public void setSidebarOpacity(double sidebarOpacity) {
            UI_SIDEBAR_OPACITY.set(sidebarOpacity);
        }
    }

    public void saveConfig() {}

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
        UI_SIDEBAR_WIDTH = BUILDER.defineInRange("Sidebar Width", 200, 130, 270);
        UI_SIDEBAR_OPACITY = BUILDER.defineInRange("Sidebar Opacity", 0.5, 0.0, 1.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}