package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
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
import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;

    private static MapRenderingOptionsSidebar INSTANCE;
    private final SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService<?>>> mapSourceDropdown;
    private final SidebarElementListComponent tmsPropertyElementList;
    private SidebarNumberInput yAxisInput;

    public MapRenderingOptionsSidebar() {
        super(
                BTETerraRendererConfig.UIConfig.INSTANCE.getSidebarSide(),
                20, 20, ELEMENT_DISTANCE,
                PropertyAccessor.of(
                        BTETerraRendererConfig.UIConfig.INSTANCE::getSidebarWidth,
                        BTETerraRendererConfig.UIConfig.INSTANCE::setSidebarWidth
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
        BTETerraRendererConfig config = BTETerraRendererConfig.INSTANCE;
        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HologramConfig.INSTANCE;

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                PropertyAccessor.of(boolean.class, config::isDoRender, config::setDoRender),
                I18nManager.format("gui.bteterrarenderer.settings.map_rendering") + ": "
        );
        SidebarSlider<Double> opacitySlider = new SidebarSlider<>(
                RangedDoublePropertyAccessor.of(renderSettings::getOpacity, renderSettings::setOpacity, 0, 1),
                I18nManager.format("gui.bteterrarenderer.settings.opacity") + ": ", ""
        );

        // Map source components
        SidebarButton reloadMapsButton = new SidebarButton(
                I18nManager.format("gui.bteterrarenderer.settings.map_reload"),
                (self, mouseButton) -> this.reloadMaps()
        );
        SidebarButton openMapsFolderButton = new SidebarButton(
                I18nManager.format("gui.bteterrarenderer.settings.map_folder"),
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

        this.setComponents(
                // ===========================================================================================
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.title"), SidebarText.TextAlign.CENTER),
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.general"), SidebarText.TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                yAxisInput,
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
        return BTETerraRendererConfig.INSTANCE.getTileMapService();
    }

    private void setTileMapService(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        TileMapService<?> tms = tmsWrapped == null ? null : tmsWrapped.getItem();

        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HologramConfig.INSTANCE;

        if(tms == null) {
            if(this.yAxisInput != null) this.yAxisInput.hide = true;
            this.tmsPropertyElementList.hide = true;
            return;
        }

        BTETerraRendererConfig.INSTANCE.setTileMapService(tmsWrapped);
        this.tmsPropertyElementList.setProperties(tms.getProperties());
        this.tmsPropertyElementList.hide = false;

        if(tms instanceof FlatTileMapService) {
            this.yAxisInput = new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getFlatMapYAxis, renderSettings::setFlatMapYAxis),
                    I18nManager.format("gui.bteterrarenderer.settings.map_y_level") + ": ");
        } else {
            this.yAxisInput = new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getYAlign, renderSettings::setYAlign),
                    I18nManager.format("gui.bteterrarenderer.settings.y_align") + ": ");
        }
    }

    private void reloadMaps() {
        try {
            BTETerraRendererConfig.INSTANCE.refreshTileMapService();
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
        if("default".equalsIgnoreCase(tmsWrapped.getSource())) {
            return tms.getName();
        }
        return "[§7" + tmsWrapped.getSource() + "§r]\n§r" + tms.getName();
    }

    public static void open() {
        if(INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        BTETerraRendererConfig.INSTANCE.load();
        INSTANCE.setSide(BTETerraRendererConfig.UIConfig.INSTANCE.getSidebarSide());
        INSTANCE.updateMapSourceDropdown();
        INSTANCE.setTileMapService(BTETerraRendererConfig.INSTANCE.getTileMapService());
        RawGuiManager.displayGuiScreen(INSTANCE);
    }

    @Override
    public void onClose() {
        BTETerraRendererConfig.INSTANCE.save();
        super.onClose();
    }
}
