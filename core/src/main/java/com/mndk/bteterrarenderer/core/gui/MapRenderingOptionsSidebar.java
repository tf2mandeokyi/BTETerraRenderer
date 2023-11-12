package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElementWrapper;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.util.i18n.I18nManager;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.SidebarText;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarElementListComponent;
import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.SidebarHorizontalLine;
import com.mndk.bteterrarenderer.core.gui.sidebar.dropdown.SidebarDropdownSelector;
import com.mndk.bteterrarenderer.core.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.mapaligner.SidebarMapAligner;
import com.mndk.bteterrarenderer.core.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;

import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;

    private static MapRenderingOptionsSidebar INSTANCE;
    private final SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService<?>>> mapSourceDropdown;
    private final SidebarElementListComponent tmsPropertyElementList;
    private final GuiSidebarElementWrapper yAxisInputWrapper;

    public MapRenderingOptionsSidebar() {
        super(
                BTETerraRendererConfig.UI.getSidebarSide(),
                20, 20, ELEMENT_DISTANCE,
                PropertyAccessor.of(
                        BTETerraRendererConfig.UI::getSidebarWidth,
                        BTETerraRendererConfig.UI::setSidebarWidth
                ), false
        );

        // Map source components
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                PropertyAccessor.of(BTRUtil.uncheckedCast(CategoryMap.Wrapper.class),
                        this::getWrappedTMS, this::setTileMapServiceWrapper),
                MapRenderingOptionsSidebar::tmsWrappedToString
        );
        // Don't make sound, as the main list of the sidebar already makes it
        this.tmsPropertyElementList = new SidebarElementListComponent(ELEMENT_DISTANCE, false);
        this.yAxisInputWrapper = new GuiSidebarElementWrapper();
    }

    @Override
    public void initGui() {
        BTETerraRendererConfig.HologramConfig hologramSettings = BTETerraRendererConfig.HOLOGRAM;

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                PropertyAccessor.of(boolean.class, hologramSettings::isDoRender, hologramSettings::setDoRender),
                I18nManager.format("gui.bteterrarenderer.settings.map_rendering") + ": "
        );
        SidebarSlider<Double> opacitySlider = new SidebarSlider<>(
                RangedDoublePropertyAccessor.of(hologramSettings::getOpacity, hologramSettings::setOpacity, 0, 1),
                I18nManager.format("gui.bteterrarenderer.settings.opacity") + ": ", ""
        );

        // Map source components
        SidebarButton reloadMapsButton = new SidebarButton(
                I18nManager.format("gui.bteterrarenderer.settings.map_reload"),
                (self, mouseButton) -> this.reloadMapSources()
        );
        SidebarButton openMapsFolderButton = new SidebarButton(
                I18nManager.format("gui.bteterrarenderer.settings.map_folder"),
                (self, mouseButton) -> this.openMapsFolder()
        );

        // Map orientation components
        SidebarMapAligner mapAligner = new SidebarMapAligner(
                PropertyAccessor.of(double.class, hologramSettings::getXAlign, hologramSettings::setXAlign),
                PropertyAccessor.of(double.class, hologramSettings::getZAlign, hologramSettings::setZAlign),
                PropertyAccessor.of(boolean.class, hologramSettings::isLockNorth, hologramSettings::setLockNorth)
        );

        SidebarBlank blank = new SidebarBlank(10);
        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        this.setComponents(
                // ===========================================================================================
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.title"), SidebarText.TextAlign.CENTER),
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.general"), SidebarText.TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                this.yAxisInputWrapper,
                opacitySlider,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_source"), SidebarText.TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                this.mapSourceDropdown,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_settings"), SidebarText.TextAlign.LEFT),
                this.tmsPropertyElementList,
                blank,

                reloadMapsButton,
                openMapsFolderButton,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_offset"), SidebarText.TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                mapAligner
                // ===========================================================================================
        );

        super.initGui();
    }

    private CategoryMap.Wrapper<TileMapService<?>> getWrappedTMS() {
        return BTETerraRendererConfig.getTileMapServiceWrapper();
    }

    private void setTileMapServiceWrapper(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        TileMapService<?> tms = tmsWrapped == null ? null : tmsWrapped.getItem();

        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HOLOGRAM;

        if(tms == null) {
            this.yAxisInputWrapper.hide = true;
            this.tmsPropertyElementList.hide = true;
            return;
        }

        BTETerraRendererConfig.setTileMapService(tmsWrapped);
        this.tmsPropertyElementList.setProperties(tms.getProperties());
        this.tmsPropertyElementList.hide = false;

        if(tms instanceof FlatTileMapService) {
            this.yAxisInputWrapper.setElement(new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getFlatMapYAxis, renderSettings::setFlatMapYAxis),
                    I18nManager.format("gui.bteterrarenderer.settings.map_y_level") + ": "));
        } else {
            this.yAxisInputWrapper.setElement(new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getYAlign, renderSettings::setYAlign),
                    I18nManager.format("gui.bteterrarenderer.settings.y_align") + ": "));
        }
    }

    private void reloadMapSources() {
        try {
            ConfigLoaders.loadAll(false);
            this.updateMapSourceDropdown();
        } catch(Exception e) {
            MinecraftClientManager.sendErrorMessageToChat("Error reloading maps!", e);
        }
    }

    private void updateMapSourceDropdown() {
        SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService<?>>>.ItemListUpdater updater =
                mapSourceDropdown.itemListBuilder();

        CategoryMap<TileMapService<?>> tmsCategoryMap = TileMapServiceYamlLoader.INSTANCE.getResult();
        for(Map.Entry<String, CategoryMap.Category<TileMapService<?>>> categoryEntry : tmsCategoryMap.getCategories()) {
            updater.push(categoryEntry.getKey());
            categoryEntry.getValue().values().forEach(updater::add);
            updater.pop();
        }
        updater.update();
    }

    private void openMapsFolder() {
        try {
            File directory = TileMapServiceYamlLoader.INSTANCE.getMapFilesDirectory();
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory);
                return;
            }

            Process p = new ProcessBuilder("explorer.exe", "/select," + directory.getAbsolutePath()).start();
            p.waitFor(3, TimeUnit.SECONDS);
            p.destroy();
        } catch(Exception e) {
            MinecraftClientManager.sendErrorMessageToChat("Error opening the config folder!", e);
        }
    }

    private static String tmsWrappedToString(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        TileMapService<?> tms = tmsWrapped.getItem();
        if(tms == null) {
            return "[§7" + tmsWrapped.getSource() + "§r]\n§4§o(error)";
        }
        if("default".equalsIgnoreCase(tmsWrapped.getSource())) {
            return tms.getName();
        }
        return "[§7" + tmsWrapped.getSource() + "§r]\n§r" + tms.getName();
    }

    public static void open() {
        if(INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        BTETerraRendererConfig.load();
        INSTANCE.setSide(BTETerraRendererConfig.UI.getSidebarSide());
        INSTANCE.updateMapSourceDropdown();
        INSTANCE.setTileMapServiceWrapper(BTETerraRendererConfig.getTileMapServiceWrapper());
        RawGuiManager.displayGuiScreen(INSTANCE);
    }

    @Override
    public void onClose() {
        BTETerraRendererConfig.save();
        super.onClose();
    }
}
