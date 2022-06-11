package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.chat.ErrorMessageHandler;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.gui.sidebar.elem.*;
import com.mndk.bteterrarenderer.tile.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mndk.bteterrarenderer.BTETerraRendererConfig.RENDER_SETTINGS;
import static com.mndk.bteterrarenderer.BTETerraRendererConfig.UI_SETTINGS;

public class MapRenderingOptionsSidebar extends GuiSidebar {


    private static MapRenderingOptionsSidebar instance;

    private SidebarDropdownSelector<TileMapService> dropdown;

    public MapRenderingOptionsSidebar() {
        super(
                UI_SETTINGS.sidebarSide,
                20, 20, 7,
                GetterSetter.from(UI_SETTINGS::getSidebarWidth, UI_SETTINGS::setSidebarWidth)
        );

        SidebarBlank blank = new SidebarBlank(10);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        if(dropdown == null) {
            dropdown = new SidebarDropdownSelector<>(
                    GetterSetter.from(BTETerraRendererConfig::getTileMapService, BTETerraRendererConfig::setTileMapService),
                    TileMapService::getName
            );
            dropdown.addCategories(TMSYamlLoader.result.getCategories());
        }

        this.elements.addAll(Arrays.asList(

                // ===========================================================================================
                new SidebarText("BTETerraRenderer Settings", SidebarText.TextAlignment.CENTER),
                // ===========================================================================================
                blank,
                new SidebarText("General", SidebarText.TextAlignment.LEFT),
                hl,
                // -------------------------------------------------------------------------------------------
                new SidebarBooleanButton(
                        GetterSetter.from(
                                () -> BTETerraRendererConfig.doRender,
                                value -> BTETerraRendererConfig.doRender = value
                        ),
                        I18n.format("gui.bteterrarenderer.settings.map_rendering") + ": "
                ),
                new SidebarNumberInput(
                        GetterSetter.from(RENDER_SETTINGS::getYAxis, RENDER_SETTINGS::setYAxis),
                        I18n.format("gui.bteterrarenderer.settings.map_y_level") + ": "
                ),
                new SidebarSlider(
                        GetterSetter.from(RENDER_SETTINGS::getOpacity, RENDER_SETTINGS::setOpacity),
                        I18n.format("gui.bteterrarenderer.settings.opacity") + ": ", "",
                        0, 1
                ),
                // ===========================================================================================
                blank,
                new SidebarText("Map Source", SidebarText.TextAlignment.LEFT),
                hl,
                // -------------------------------------------------------------------------------------------
                dropdown,
                new SidebarButton(
                        "Reload maps...",
                        (self, mouseButton) -> {
                            try {
                                Map<String, Boolean> opened = new HashMap<>();
                                for(SidebarDropdownCategory<TileMapService> category : dropdown.getCategories()) {
                                    opened.put(category.getName(), category.isOpened());
                                }
                                TMSYamlLoader.refresh();
                                dropdown.clearCategories();
                                dropdown.addCategories(TMSYamlLoader.result.getCategories());
                                for(SidebarDropdownCategory<TileMapService> category : dropdown.getCategories()) {
                                    if(opened.get(category.getName())) {
                                        category.setOpened(true);
                                    }
                                }
                            } catch(Exception e) {
                                ErrorMessageHandler.sendToClient("Error while reloading maps! Reason: " + e.getMessage());
                            }
                        }
                ),
                new SidebarButton(
                        "Open maps folder...",
                        (self, mouseButton) -> {
                            try {
                                if(Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(TMSYamlLoader.getMapFilesDirectory());
                                }
                            } catch(Exception ignored) {}
                        }
                ),
                // ===========================================================================================
                blank,
                new SidebarText("Map Offset", SidebarText.TextAlignment.LEFT),
                hl,
                // -------------------------------------------------------------------------------------------
                new SidebarSlider(
                        GetterSetter.from(RENDER_SETTINGS::getRadius, RENDER_SETTINGS::setRadius),
                        I18n.format("gui.bteterrarenderer.settings.size") + ": ", "",
                        1, 8
                ),
                new SidebarSlider(
                        GetterSetter.from(RENDER_SETTINGS::getZoom, RENDER_SETTINGS::setZoom),
                        I18n.format("gui.bteterrarenderer.settings.zoom") + ": ", "",
                        -3, 3
                ),
                new SidebarMapAligner(
                        GetterSetter.from(RENDER_SETTINGS::getXAlign, RENDER_SETTINGS::setXAlign),
                        GetterSetter.from(RENDER_SETTINGS::getZAlign, RENDER_SETTINGS::setZAlign)
                )
                // ===========================================================================================
        ));
    }


    public static void open() {
        if(instance == null) instance = new MapRenderingOptionsSidebar();
        instance.setSide(UI_SETTINGS.sidebarSide);
        Minecraft.getMinecraft().displayGuiScreen(instance);
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
