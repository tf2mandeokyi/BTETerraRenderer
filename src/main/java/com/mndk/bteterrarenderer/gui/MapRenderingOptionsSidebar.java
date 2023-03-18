package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.chat.ErrorMessageHandler;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.mapaligner.SidebarMapAligner;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarHorizontalLine;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText.TextAlign;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.DropdownCategory;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.SidebarDropdownSelector;
import com.mndk.bteterrarenderer.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
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

    private final SidebarDropdownSelector<TileMapService> mapSourceDropdown;
    
    
    public MapRenderingOptionsSidebar() {
        super(
                UI_SETTINGS.getSidebarSide(),
                20, 20, 7,
                GetterSetter.from(UI_SETTINGS::getSidebarWidth, UI_SETTINGS::setSidebarWidth)
        );

        SidebarBlank blank = new SidebarBlank(10);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                GetterSetter.from(BTETerraRendererConfig::isDoRender, BTETerraRendererConfig::setDoRender),
                I18n.format("gui.bteterrarenderer.new_settings.map_rendering") + ": "
        );
        SidebarNumberInput yLevelInput = new SidebarNumberInput(
                GetterSetter.from(RENDER_SETTINGS::getYAxis, RENDER_SETTINGS::setYAxis),
                I18n.format("gui.bteterrarenderer.new_settings.map_y_level") + ": "
        );
        SidebarSlider opacitySlider = new SidebarSlider(
                GetterSetter.from(RENDER_SETTINGS::getOpacity, RENDER_SETTINGS::setOpacity),
                I18n.format("gui.bteterrarenderer.new_settings.opacity") + ": ", "",
                0, 1
        );

        // Map source components
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                GetterSetter.from(TMSYamlLoader.INSTANCE::getResult, TMSYamlLoader.INSTANCE::setResult),
                BTETerraRendererConfig::getMapServiceCategory,
                BTETerraRendererConfig::getMapServiceId,
                BTETerraRendererConfig::setTileMapService,
                tms -> "default".equalsIgnoreCase(tms.getSource()) ?
                        tms.getName() : "[§7" + tms.getSource() + "§r]\n§r" + tms.getName()
        );
        SidebarButton reloadMapsButton = new SidebarButton(
                I18n.format("gui.bteterrarenderer.new_settings.map_reload"),
                (self, mouseButton) -> this.reloadMaps()
        );
        SidebarButton openMapsFolderButton = new SidebarButton(
                I18n.format("gui.bteterrarenderer.new_settings.map_folder"),
                (self, mouseButton) -> this.openMapsFolder()
        );

        // Map orientation components
        SidebarSlider mapSizeSlider = new SidebarSlider(
                GetterSetter.from(RENDER_SETTINGS::getRadius, RENDER_SETTINGS::setRadius),
                I18n.format("gui.bteterrarenderer.new_settings.size") + ": ", "",
                1, 8,
                z -> true
        );
        SidebarSlider mapZoomSlider = new SidebarSlider(
                GetterSetter.from(RENDER_SETTINGS::getZoom, RENDER_SETTINGS::setZoom),
                I18n.format("gui.bteterrarenderer.new_settings.zoom") + ": ", "",
                -3, 3,
                BTETerraRendererConfig::isRelativeZoomAvailable
        );
        SidebarMapAligner mapAligner = new SidebarMapAligner(
                GetterSetter.from(RENDER_SETTINGS::getXAlign, RENDER_SETTINGS::setXAlign),
                GetterSetter.from(RENDER_SETTINGS::getZAlign, RENDER_SETTINGS::setZAlign),
                GetterSetter.from(RENDER_SETTINGS::isLockNorth, RENDER_SETTINGS::setLockNorth)
        );


        this.elements.addAll(Arrays.asList(
                // ===========================================================================================
                new SidebarText(I18n.format("gui.bteterrarenderer.new_settings.title"), TextAlign.CENTER),
                blank,

                new SidebarText(I18n.format("gui.bteterrarenderer.new_settings.general"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                yLevelInput,
                opacitySlider,
                blank,

                new SidebarText(I18n.format("gui.bteterrarenderer.new_settings.map_source"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                this.mapSourceDropdown,
                reloadMapsButton,
                openMapsFolderButton,
                blank,

                new SidebarText(I18n.format("gui.bteterrarenderer.new_settings.map_offset"), TextAlign.LEFT),
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

            ProjectionYamlLoader.INSTANCE.refresh();
            TMSYamlLoader.INSTANCE.refresh();
            BTETerraRendererConfig.refreshTileMapService();

            categoryMap = mapSourceDropdown.getCurrentCategories().getCategoryMap();
            for(Map.Entry<String, DropdownCategory<TileMapService>> categoryEntry : categoryMap.entrySet()) {
                if(opened.get(categoryEntry.getKey())) {
                    categoryEntry.getValue().setOpened(true);
                }
            }
        } catch(Exception e) {
            ErrorMessageHandler.sendToClient("Caught an error while reloading maps! Reason: " + e.getMessage());
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
        if(instance == null) instance = new MapRenderingOptionsSidebar();
        instance.setSide(UI_SETTINGS.getSidebarSide());
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
