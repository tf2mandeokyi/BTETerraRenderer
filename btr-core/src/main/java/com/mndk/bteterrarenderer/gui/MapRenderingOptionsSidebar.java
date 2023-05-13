package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.connector.minecraft.I18nConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarHorizontalLine;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText.TextAlign;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.DropdownCategory;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.SidebarDropdownSelector;
import com.mndk.bteterrarenderer.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.mapaligner.SidebarMapAligner;
import com.mndk.bteterrarenderer.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static MapRenderingOptionsSidebar INSTANCE;
    private final SidebarDropdownSelector<TileMapService> mapSourceDropdown;

    public MapRenderingOptionsSidebar() {
        super(
                BTRConfigConnector.INSTANCE.getUiSettings().getSidebarSide(),
                20, 20, 7,
                GetterSetter.from(
                        BTRConfigConnector.INSTANCE.getUiSettings()::getSidebarWidth,
                        BTRConfigConnector.INSTANCE.getUiSettings()::setSidebarWidth
                ), false
        );
        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector renderSettings = config.getRenderSettings();

        I18nConnector i18n = I18nConnector.INSTANCE;

        SidebarBlank blank = new SidebarBlank(10);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                GetterSetter.from(config::isDoRender, config::setDoRender),
                i18n.format("gui.bteterrarenderer.new_settings.map_rendering") + ": "
        );
        SidebarNumberInput yLevelInput = new SidebarNumberInput(
                GetterSetter.from(renderSettings::getYAxis, renderSettings::setYAxis),
                i18n.format("gui.bteterrarenderer.new_settings.map_y_level") + ": "
        );
        SidebarSlider opacitySlider = new SidebarSlider(
                GetterSetter.from(renderSettings::getOpacity, renderSettings::setOpacity),
                i18n.format("gui.bteterrarenderer.new_settings.opacity") + ": ", "",
                0, 1
        );

        // Map source components
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                GetterSetter.from(TMSYamlLoader.INSTANCE::getResult, TMSYamlLoader.INSTANCE::setResult),
                config::getMapServiceCategory,
                config::getMapServiceId,
                BTRConfigConnector::setTileMapService,
                tms -> "default".equalsIgnoreCase(tms.getSource()) ?
                        tms.getName() : "[§7" + tms.getSource() + "§r]\n§r" + tms.getName()
        );
        SidebarButton reloadMapsButton = new SidebarButton(
                i18n.format("gui.bteterrarenderer.new_settings.map_reload"),
                (self, mouseButton) -> this.reloadMaps()
        );
        SidebarButton openMapsFolderButton = new SidebarButton(
                i18n.format("gui.bteterrarenderer.new_settings.map_folder"),
                (self, mouseButton) -> this.openMapsFolder()
        );

        // Map orientation components
        SidebarSlider mapSizeSlider = new SidebarSlider(
                GetterSetter.from(renderSettings::getRadius, renderSettings::setRadius),
                i18n.format("gui.bteterrarenderer.new_settings.size") + ": ", "",
                1, 8,
                z -> true
        );
        SidebarSlider mapZoomSlider = new SidebarSlider(
                GetterSetter.from(renderSettings::getRelativeZoomValue, renderSettings::setRelativeZoom),
                i18n.format("gui.bteterrarenderer.new_settings.zoom") + ": ", "",
                -3, 3,
                BTRConfigConnector::isRelativeZoomAvailable
        );
        SidebarMapAligner mapAligner = new SidebarMapAligner(
                GetterSetter.from(renderSettings::getXAlign, renderSettings::setXAlign),
                GetterSetter.from(renderSettings::getZAlign, renderSettings::setZAlign),
                GetterSetter.from(renderSettings::isLockNorth, renderSettings::setLockNorth)
        );


        this.elements.addAll(Arrays.asList(
                // ===========================================================================================
                new SidebarText(i18n.format("gui.bteterrarenderer.new_settings.title"), TextAlign.CENTER),
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.new_settings.general"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                yLevelInput,
                opacitySlider,
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.new_settings.map_source"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                this.mapSourceDropdown,
                reloadMapsButton,
                openMapsFolderButton,
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.new_settings.map_offset"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                mapSizeSlider,
                mapZoomSlider,
                mapAligner
                // ===========================================================================================
        ));
    }

    private void reloadMaps() {
        try {
            Map<String, Boolean> opened = new HashMap<>();
            Map<String, DropdownCategory<TileMapService>> categoryMap = mapSourceDropdown.getCurrentCategories().getCategoryMap();
            for(Map.Entry<String, DropdownCategory<TileMapService>> categoryEntry : categoryMap.entrySet()) {
                opened.put(categoryEntry.getKey(), categoryEntry.getValue().isOpened());
            }

            BTRConfigConnector.refreshTileMapService();

            categoryMap = mapSourceDropdown.getCurrentCategories().getCategoryMap();
            for(Map.Entry<String, DropdownCategory<TileMapService>> categoryEntry : categoryMap.entrySet()) {
                if(opened.get(categoryEntry.getKey())) {
                    categoryEntry.getValue().setOpened(true);
                }
            }
        } catch(Exception e) {
            MinecraftClientConnector.INSTANCE.sendErrorMessageToChat("Caught an error while reloading maps! Reason: " + e.getMessage());
        }
    }

    private void openMapsFolder() {
        try {
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(TMSYamlLoader.INSTANCE.getMapFilesDirectory());
            }
        } catch(Exception ignored) {}
    }

    public static void open() {
        if(INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        INSTANCE.setSide(BTRConfigConnector.INSTANCE.getUiSettings().getSidebarSide());
        GuiStaticConnector.INSTANCE.displayGuiScreen(INSTANCE);
    }

    @Override
    public void onGuiClosed() {
        BTRConfigConnector.save();
        super.onGuiClosed();
    }

}
