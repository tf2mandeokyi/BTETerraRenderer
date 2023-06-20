package com.mndk.bteterrarenderer.gui;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.I18nConnector;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarElementListComponent;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarHorizontalLine;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText;
import com.mndk.bteterrarenderer.gui.sidebar.decorator.SidebarText.TextAlign;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.SidebarDropdownSelector;
import com.mndk.bteterrarenderer.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.mapaligner.SidebarMapAligner;
import com.mndk.bteterrarenderer.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.loader.CategoryMap;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.FlatTileMapService;
import com.mndk.bteterrarenderer.tile.TileMapService;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import com.mndk.bteterrarenderer.util.RangedDoublePropertyAccessor;
import lombok.var;

import java.awt.*;
import java.util.Arrays;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;

    private static MapRenderingOptionsSidebar INSTANCE;
    private final SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService>> mapSourceDropdown;
    private final SidebarElementListComponent tmsPropertyElementList;
    private SidebarNumberInput yAxisInput;

    public MapRenderingOptionsSidebar() {
        super(
                BTRConfigConnector.INSTANCE.getUiSettings().getSidebarSide(),
                20, 20, ELEMENT_DISTANCE,
                PropertyAccessor.of(
                        BTRConfigConnector.INSTANCE.getUiSettings()::getSidebarWidth,
                        BTRConfigConnector.INSTANCE.getUiSettings()::setSidebarWidth
                ), false
        );

        // Map source components
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                PropertyAccessor.of(BtrUtil.uncheckedCast(CategoryMap.Wrapper.class),
                        this::getWrappedTMS, this::setTileMapService),
                MapRenderingOptionsSidebar::tmsWrappedToString
        );
        this.tmsPropertyElementList = new SidebarElementListComponent(ELEMENT_DISTANCE);
    }

    @Override
    public void initGui() {
        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector renderSettings = config.getRenderSettings();
        I18nConnector i18n = I18nConnector.INSTANCE;

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                PropertyAccessor.of(boolean.class, config::isDoRender, config::setDoRender),
                i18n.format("gui.bteterrarenderer.settings.map_rendering") + ": "
        );
        SidebarSlider<Double> opacitySlider = new SidebarSlider<>(
                RangedDoublePropertyAccessor.of(renderSettings::getOpacity, renderSettings::setOpacity, 0, 1),
                i18n.format("gui.bteterrarenderer.settings.opacity") + ": ", ""
        );

        // Map source components
        SidebarButton reloadMapsButton = new SidebarButton(
                i18n.format("gui.bteterrarenderer.settings.map_reload"),
                (self, mouseButton) -> this.reloadMaps()
        );
        SidebarButton openMapsFolderButton = new SidebarButton(
                i18n.format("gui.bteterrarenderer.settings.map_folder"),
                (self, mouseButton) -> this.openMapsFolder()
        );

        // Map orientation components
        SidebarMapAligner mapAligner = new SidebarMapAligner(
                PropertyAccessor.of(double.class, renderSettings::getXAlign, renderSettings::setXAlign),
                PropertyAccessor.of(double.class, renderSettings::getZAlign, renderSettings::setZAlign),
                PropertyAccessor.of(boolean.class, renderSettings::isLockNorth, renderSettings::setLockNorth)
        );

        SidebarBlank blank = new SidebarBlank(10);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        this.elementsComponent.set(Arrays.asList(
                // ===========================================================================================
                new SidebarText(i18n.format("gui.bteterrarenderer.settings.title"), TextAlign.CENTER),
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.settings.general"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                yAxisInput,
                opacitySlider,
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.settings.map_source"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                this.mapSourceDropdown,
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.settings.map_settings"), TextAlign.LEFT),
                this.tmsPropertyElementList,
                blank,

                reloadMapsButton,
                openMapsFolderButton,
                blank,

                new SidebarText(i18n.format("gui.bteterrarenderer.settings.map_offset"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                mapAligner
                // ===========================================================================================
        ));

        super.initGui();
    }

    private CategoryMap.Wrapper<TileMapService> getWrappedTMS() {
        return BTRConfigConnector.getTileMapService();
    }

    private void setTileMapService(CategoryMap.Wrapper<TileMapService> tmsWrapped) {
        TileMapService tms = tmsWrapped == null ? null : tmsWrapped.getValue();

        BTRConfigConnector.RenderSettingsConnector renderSettings = BTRConfigConnector.INSTANCE.getRenderSettings();
        I18nConnector i18n = I18nConnector.INSTANCE;

        if(tms == null) {
            if(this.yAxisInput != null) this.yAxisInput.hide = true;
            this.tmsPropertyElementList.hide = true;
            return;
        }

        BTRConfigConnector.setTileMapService(tmsWrapped);
        this.tmsPropertyElementList.setProperties(tms.getProperties());
        this.tmsPropertyElementList.hide = false;

        if(tms instanceof FlatTileMapService) {
            this.yAxisInput = new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getFlatMapYAxis, renderSettings::setFlatMapYAxis),
                    i18n.format("gui.bteterrarenderer.settings.map_y_level") + ": ");
        } else {
            this.yAxisInput = new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getYAlign, renderSettings::setYAlign),
                    i18n.format("gui.bteterrarenderer.settings.y_align") + ": ");
        }
    }

    private void reloadMaps() {
        try {
            BTRConfigConnector.refreshTileMapService();
            this.updateMapSourceDropdown();
        } catch(Exception e) {
            MinecraftClientConnector.INSTANCE.sendErrorMessageToChat("Error reloading maps!", e);
        }
    }

    private void updateMapSourceDropdown() {
        var updater = mapSourceDropdown.itemListBuilder();

        CategoryMap<TileMapService> tmsCategoryMap = TMSYamlLoader.INSTANCE.getResult();
        for(var categoryEntry : tmsCategoryMap.entrySet()) {
            updater.push(categoryEntry.getKey());
            categoryEntry.getValue().values().forEach(updater::add);
            updater.pop();
        }
        updater.update();
    }

    private void openMapsFolder() {
        try {
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(TMSYamlLoader.INSTANCE.getMapFilesDirectory());
            }
        } catch(Exception e) {
            MinecraftClientConnector.INSTANCE.sendErrorMessageToChat("Error opening the maps folder!", e);
        }
    }

    private static String tmsWrappedToString(CategoryMap.Wrapper<TileMapService> tmsWrapped) {
        TileMapService tms = tmsWrapped.getValue();
        if("default".equalsIgnoreCase(tmsWrapped.getSource())) {
            return tms.getName();
        }
        return "[§7" + tmsWrapped.getSource() + "§r]\n§r" + tms.getName();
    }

    public static void open() {
        if(INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        BTRConfigConnector.load();
        INSTANCE.setSide(BTRConfigConnector.INSTANCE.getUiSettings().getSidebarSide());
        INSTANCE.updateMapSourceDropdown();
        INSTANCE.setTileMapService(BTRConfigConnector.getTileMapService());
        GuiStaticConnector.INSTANCE.displayGuiScreen(INSTANCE);
    }

    @Override
    public void onClose() {
        BTRConfigConnector.save();
        super.onClose();
    }
}
