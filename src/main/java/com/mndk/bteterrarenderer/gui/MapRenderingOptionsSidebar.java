package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.gui.sidebar.elem.*;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;
import com.mndk.bteterrarenderer.tms.TileMapService;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapRenderingOptionsSidebar extends GuiSidebar {


    private static SidebarDropdownSelector<TileMapService> dropdown;


    public MapRenderingOptionsSidebar() {
        super(
                BTETerraRendererConfig.UI_SETTINGS.sidebarSide,
                20, 20, 10,
                GetterSetter.from(
                        BTETerraRendererConfig.UI_SETTINGS::getSidebarWidth,
                        BTETerraRendererConfig.UI_SETTINGS::setSidebarWidth
                )
        );

        SidebarBlank blank = new SidebarBlank(13);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        dropdown = new SidebarDropdownSelector<>(
                GetterSetter.from(
                        BTETerraRendererConfig::getTileMapService,
                        BTETerraRendererConfig::setTileMapService
                ),
                TileMapService::getName
        );
        dropdown.addCategories(TileMapYamlLoader.result.getCategories());

        this.elements.addAll(Arrays.asList(
                new SidebarText("BTETerraRenderer Settings", SidebarText.TextAlignment.CENTER),
                blank,
                new SidebarText("General", SidebarText.TextAlignment.LEFT),
                hl,
                new SidebarBooleanButton(
                        GetterSetter.from(
                                () -> BTETerraRendererConfig.doRender,
                                value -> BTETerraRendererConfig.doRender = value
                        ),
                        I18n.format("gui.bteterrarenderer.settings.map_rendering") + ": "
                ),
                new SidebarNumberInput(
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getYAxis,
                                BTETerraRendererConfig.RENDER_SETTINGS::setYAxis
                        ),
                        I18n.format("gui.bteterrarenderer.settings.map_y_level") + ": "
                ),
                new SidebarSlider(
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getOpacity,
                                BTETerraRendererConfig.RENDER_SETTINGS::setOpacity
                        ),
                        I18n.format("gui.bteterrarenderer.settings.opacity") + ": ", "",
                        0, 1
                ),
                blank,
                new SidebarText("Map Source", SidebarText.TextAlignment.LEFT),
                hl,
                dropdown,
                new SidebarButton(
                        "Reload maps...",
                        (self, mouseButton) -> {
                            try {
                                Map<String, Boolean> opened = new HashMap<>();
                                for(SidebarDropdownCategory<TileMapService> category : dropdown.getCategories()) {
                                    opened.put(category.getName(), category.isOpened());
                                }
                                TileMapYamlLoader.refresh();
                                dropdown.clearCategories();
                                dropdown.addCategories(TileMapYamlLoader.result.getCategories());
                                for(SidebarDropdownCategory<TileMapService> category : dropdown.getCategories()) {
                                    if(opened.get(category.getName())) {
                                        category.setOpened(true);
                                    }
                                }
                            } catch(Exception e) {
                                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
                                        "Error while reloading maps! Reason: " + e.getMessage()
                                ));
                            }
                        }
                ),
                blank,
                new SidebarText("Map Offset", SidebarText.TextAlignment.LEFT),
                hl,
                new SidebarSlider(
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getRadius,
                                BTETerraRendererConfig.RENDER_SETTINGS::setRadius
                        ),
                        I18n.format("gui.bteterrarenderer.settings.size") + ": ", "",
                        1, 8
                ),
                new SidebarSlider(
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getZoom,
                                BTETerraRendererConfig.RENDER_SETTINGS::setZoom
                        ),
                        I18n.format("gui.bteterrarenderer.settings.zoom") + ": ", "",
                        -3, 3
                ),
                new SidebarMapAligner(
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getXAlign,
                                BTETerraRendererConfig.RENDER_SETTINGS::setXAlign
                        ),
                        GetterSetter.from(
                                BTETerraRendererConfig.RENDER_SETTINGS::getZAlign,
                                BTETerraRendererConfig.RENDER_SETTINGS::setZAlign
                        )
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
