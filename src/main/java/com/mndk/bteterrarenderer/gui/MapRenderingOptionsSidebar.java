package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.gui.sidebar.*;
import com.mndk.bteterrarenderer.gui.sidebar.elem.*;
import com.mndk.bteterrarenderer.storage.TileMapLoaderResult;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;
import com.mndk.bteterrarenderer.tms.TileMapService;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapRenderingOptionsSidebar extends GuiSidebar {



    public MapRenderingOptionsSidebar() {
        super(
                BTETerraRendererConfig.UI_SETTINGS.sidebarSide,
                20, 20, 10,
                GetterSetter.from(
                        () -> BTETerraRendererConfig.UI_SETTINGS.sidebarWidth,
                        (value) -> BTETerraRendererConfig.UI_SETTINGS.sidebarWidth = value
                )
        );

        SidebarBlank blank = new SidebarBlank(13);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        List<Object> dropdownSelectorList = new ArrayList<>();

        for(TileMapLoaderResult.Category category : TileMapYamlLoader.result.getCategories()) {
            dropdownSelectorList.add(category.getName());
            dropdownSelectorList.addAll(category.getMaps());
        }

        this.elements.addAll(Arrays.asList(
                new SidebarText("BTETerraRenderer Settings", SidebarText.TextAlignment.CENTER),

                blank, new SidebarText("General", SidebarText.TextAlignment.LEFT), hl,

                new SidebarBooleanButton(
                        GetterSetter.from(() -> BTETerraRendererConfig.doRender,
                                value -> BTETerraRendererConfig.doRender = value),
                        I18n.format("gui.bteterrarenderer.maprenderer.map_rendering") + ": "
                ),

                new SidebarNumberInput(
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.yAxis,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.yAxis = value),
                        I18n.format("gui.bteterrarenderer.maprenderer.map_y_level") + ": "
                ),

                new SidebarSlider(
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.opacity,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.opacity = value),
                        I18n.format("gui.bteterrarenderer.maprenderer.opacity") + ": ", "",
                        0, 1
                ),

                blank, new SidebarText("Map Source", SidebarText.TextAlignment.LEFT), hl,

                new SidebarDropdownSelector<>(
                        GetterSetter.from(BTETerraRendererConfig::getTileMapService,
                                BTETerraRendererConfig::setTileMapService),
                        TileMapService::getName,
                        dropdownSelectorList.toArray(new Object[0])
                ),

                blank, new SidebarText("Map Offset", SidebarText.TextAlignment.LEFT), hl,

                new SidebarSlider(
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.radius,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.radius = value),
                        I18n.format("gui.bteterrarenderer.maprenderer.size") + ": ", "",
                        1, 5
                ),

                new SidebarSlider(
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.zoom,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.zoom = value),
                        I18n.format("gui.bteterrarenderer.maprenderer.zoom") + ": ", "",
                        -3, 3
                ),

                new SidebarMapAligner(
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.align_x,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.align_x = value),
                        GetterSetter.from(() -> BTETerraRendererConfig.RENDER_SETTINGS.align_z,
                                value -> BTETerraRendererConfig.RENDER_SETTINGS.align_z = value)
                ) // TODO
        ));
    }



    @Override
    public void onGuiClosed() {
        BTETerraRendererConfig.save();
        super.onGuiClosed();
    }



    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
