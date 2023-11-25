package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.baker.URLBufferedImageBaker;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.*;
import com.mndk.bteterrarenderer.core.gui.sidebar.dropdown.SidebarDropdownSelector;
import com.mndk.bteterrarenderer.core.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.mapaligner.SidebarMapAligner;
import com.mndk.bteterrarenderer.core.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.GuiSidebarElementWrapper;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementListComponent;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.network.SimpleImageFetcher;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.i18n.I18nManager;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;

    private static final SimpleImageFetcher ICON_FETCHER = new SimpleImageFetcher(
            Executors.newCachedThreadPool(),
            -1, 100, 3, 500, false);
    private static final URLBufferedImageBaker ICON_BAKER = new URLBufferedImageBaker(-1, -1, false);

    private static MapRenderingOptionsSidebar INSTANCE;
    private SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService<?>>> mapSourceDropdown;
    private SidebarTextComponent mapCopyright;
    private SidebarElementListComponent tmsPropertyElementList;
    private GuiSidebarElementWrapper yAxisInputWrapper;

    public MapRenderingOptionsSidebar() {
        super(
                20, 20, ELEMENT_DISTANCE, false,
                PropertyAccessor.of(
                        BTETerraRendererConfig.UI::getSidebarWidth,
                        BTETerraRendererConfig.UI::setSidebarWidth
                ),
                PropertyAccessor.of(SidebarSide.class,
                        BTETerraRendererConfig.UI::getSidebarSide,
                        BTETerraRendererConfig.UI::setSidebarSide
                )
        );

        // Don't make sidebar elements here. They won't get initialized.
        // instead put all of them in getElements()
    }

    @Override
    protected List<GuiSidebarElement> getElements() {
        BTETerraRendererConfig.HologramConfig hologramSettings = BTETerraRendererConfig.HOLOGRAM;

        // General components
        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                PropertyAccessor.of(boolean.class, hologramSettings::isDoRender, hologramSettings::setDoRender),
                I18nManager.format("gui.bteterrarenderer.settings.map_rendering") + ": "
        );
        this.yAxisInputWrapper = new GuiSidebarElementWrapper();
        SidebarSlider<Double> opacitySlider = new SidebarSlider<>(
                RangedDoublePropertyAccessor.of(hologramSettings::getOpacity, hologramSettings::setOpacity, 0, 1),
                I18nManager.format("gui.bteterrarenderer.settings.opacity") + ": ", ""
        );

        // Map source components
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                PropertyAccessor.of(BTRUtil.uncheckedCast(CategoryMap.Wrapper.class),
                        this::getWrappedTMS, this::setTileMapServiceWrapper),
                MapRenderingOptionsSidebar::tmsWrappedToString,
                MapRenderingOptionsSidebar::getIconTextureObject
        );
        this.mapCopyright = new SidebarTextComponent(TextAlign.LEFT);
        this.tmsPropertyElementList = new SidebarElementListComponent(ELEMENT_DISTANCE, 0, null, false);
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

        return Arrays.asList(
                // ===========================================================================================
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.title"), TextAlign.CENTER),
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.general"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                renderingTrigger,
                this.yAxisInputWrapper,
                opacitySlider,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_source"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                this.mapSourceDropdown,
                this.mapCopyright,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_settings"), TextAlign.LEFT),
                this.tmsPropertyElementList,
                blank,

                reloadMapsButton,
                openMapsFolderButton,
                blank,

                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_offset"), TextAlign.LEFT),
                hl, // ---------------------------------------------------------------------------------------
                mapAligner
                // ===========================================================================================
        );
    }

    @Override
    protected void drawScreen(Object poseStack) {
        ICON_BAKER.process(1);
        super.drawScreen(poseStack);
    }

    private CategoryMap.Wrapper<TileMapService<?>> getWrappedTMS() {
        return BTETerraRendererConfig.getTileMapServiceWrapper();
    }

    private void setTileMapServiceWrapper(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HOLOGRAM;

        // Check null
        TileMapService<?> tms = tmsWrapped == null ? null : tmsWrapped.getItem();
        if(tms == null) {
            this.yAxisInputWrapper.hide = true;
            this.tmsPropertyElementList.hide = true;
            return;
        }

        // Set to config
        BTETerraRendererConfig.setTileMapService(tmsWrapped);

        // Set property element list
        this.tmsPropertyElementList.clear();
        this.tmsPropertyElementList.addProperties(tms.getProperties());
        this.tmsPropertyElementList.hide = false;

        // Set y axis input
        if(tms instanceof FlatTileMapService) {
            this.yAxisInputWrapper.setElement(new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getFlatMapYAxis, renderSettings::setFlatMapYAxis),
                    I18nManager.format("gui.bteterrarenderer.settings.map_y_level") + ": "));
        } else {
            this.yAxisInputWrapper.setElement(new SidebarNumberInput(
                    PropertyAccessor.of(renderSettings::getYAlign, renderSettings::setYAlign),
                    I18nManager.format("gui.bteterrarenderer.settings.y_align") + ": "));
        }

        // Set copyright
        this.mapCopyright.setTextComponentJson(tms.getCopyrightTextJson());
    }

    private void reloadMapSources() {
        try {
            BTETerraRendererConfig.load(true);
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
            CategoryMap.Category<TileMapService<?>> category = categoryEntry.getValue();
            updater.push(categoryEntry.getKey());
            category.values().forEach(updater::add);
            updater.pop();
        }
        updater.update();
    }

    private void openMapsFolder() {
        try {
            File directory = TileMapServiceYamlLoader.INSTANCE.getFilesDirectory();
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory);
                return;
            }

            Process p;
            if(System.getProperty("os.name").startsWith("Windows")) {
                p = new ProcessBuilder("explorer.exe", "/select," + directory.getAbsolutePath()).start();
            }
            else if(System.getProperty("os.name").startsWith("Mac")) {
                p = new ProcessBuilder("usr/bin/open", directory.getAbsolutePath()).start();
            }
            else {
                MinecraftClientManager.sendErrorMessageToChat("Cannot open file explorer! Instead you can manually go to:");
                MinecraftClientManager.sendErrorMessageToChat(directory.getAbsolutePath());
                return;
            }
            p.waitFor(3, TimeUnit.SECONDS);
            p.destroy();
        } catch(Exception e) {
            MinecraftClientManager.sendErrorMessageToChat("Error opening the map folder!", e);
        }
    }

    private static Object getIconTextureObject(CategoryMap.Wrapper<TileMapService<?>> wrapper) {
        TileMapService<?> tms = wrapper.getItem();
        if(tms == null) return null;

        URL iconUrl = tms.getIconUrl();
        if(iconUrl == null) return null;

        switch(ICON_BAKER.getResourceProcessingState(iconUrl)) {
            case NOT_PROCESSED:
                ICON_FETCHER.resourceProcessingReady(iconUrl, iconUrl);
                ICON_BAKER.setResourceInPreparingState(iconUrl);
                break;
            case PROCESSED:
                return ICON_BAKER.updateAndGetResource(iconUrl);
            case ERROR:
                return null;
        }

        switch(ICON_FETCHER.getResourceProcessingState(iconUrl)) {
            case PROCESSED:
                ICON_BAKER.resourceProcessingReady(iconUrl, ICON_FETCHER.updateAndGetResource(iconUrl));
                break;
            case ERROR:
                ICON_BAKER.resourcePreparingError(iconUrl, ICON_FETCHER.getResourceErrorReason(iconUrl));
                return null;
        }
        return null;
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
        BTETerraRendererConfig.save();
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
