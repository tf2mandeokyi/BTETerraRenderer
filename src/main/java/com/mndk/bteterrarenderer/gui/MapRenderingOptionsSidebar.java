package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.config.BTRConfig;
import com.mndk.bteterrarenderer.gui.sidebar.*;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.resources.I18n;

public class MapRenderingOptionsSidebar extends GuiSidebar {



    private static final GuiSidebarElement[] elements = new GuiSidebarElement[] {

			new SidebarText("BTETerraRenderer Settings"),

            new SidebarHorizontalLine(1, 0xFFFFFF),

            new SidebarText("General"),

			new SidebarBooleanButton(
			        GetterSetter.from(() -> BTRConfig.doRender,
                            value -> BTRConfig.doRender = value),
                    I18n.format("gui.bteterrarenderer.maprenderer.map_rendering") + ": "
            ),

            new SidebarNumberInput(
                    GetterSetter.from(() -> BTRConfig.RENDER_SETTINGS.yAxis,
                            value -> BTRConfig.RENDER_SETTINGS.yAxis = value),
                    I18n.format("gui.bteterrarenderer.maprenderer.map_y_level") + ": "
            ),

            new SidebarSlider(
                    GetterSetter.from(() -> BTRConfig.RENDER_SETTINGS.opacity,
                            value -> BTRConfig.RENDER_SETTINGS.opacity = value),
                    I18n.format("gui.bteterrarenderer.maprenderer.opacity") + ": ", "",
                    0, 1
            ),

            new SidebarText("(TODO: Dropdown) Change Map Source"), // TODO

            new SidebarHorizontalLine(1, 0xFFFFFF),

            new SidebarSlider(
                    GetterSetter.from(() -> BTRConfig.RENDER_SETTINGS.radius,
                            value -> BTRConfig.RENDER_SETTINGS.radius = value),
                    I18n.format("gui.bteterrarenderer.maprenderer.size") + ": ", "",
                    1, 5
            ),

            new SidebarSlider(
                    GetterSetter.from(() -> BTRConfig.RENDER_SETTINGS.zoom,
                        value -> BTRConfig.RENDER_SETTINGS.zoom = value),
                    I18n.format("gui.bteterrarenderer.maprenderer.zoom") + ": ", "",
                    -3, 3
            ),

            new SidebarHorizontalLine(1, 0xFFFFFF),

            new SidebarText("(TODO: Custom) Map Alignment") // TODO

    };



    public MapRenderingOptionsSidebar() {
        super(
                elements, BTRConfig.UI_SETTINGS.sidebarSide,
                20, 20, 10,
                GetterSetter.from(() -> BTRConfig.UI_SETTINGS.sidebarWidth, (value) -> BTRConfig.UI_SETTINGS.sidebarWidth = value)
        );
    }



    @Override
    public void onGuiClosed() {
        BTRConfig.save();
        super.onGuiClosed();
    }



    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
