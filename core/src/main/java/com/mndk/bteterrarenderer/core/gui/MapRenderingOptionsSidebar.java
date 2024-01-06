package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.ImageBakingBlock;
import com.mndk.bteterrarenderer.core.graphics.ImageResizingBlock;
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
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementWrapper;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementList;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.network.SimpleImageFetchingBlock;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.i18n.Translatable;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;
    private static final int ELEMENT_DISTANCE_BIG = 35;

    private static final IconMaker ICON_MAKER = new IconMaker();

    private static MapRenderingOptionsSidebar INSTANCE;
    private final SidebarDropdownSelector<CategoryMap.Wrapper<TileMapService<?>>> mapSourceDropdown;
    private final SidebarTextComponent mapCopyright;
    private final SidebarElementList tmsPropertyElementList;
    private final SidebarElementWrapper yAxisInputWrapper;

    public MapRenderingOptionsSidebar() {
        super(
                20, 40, ELEMENT_DISTANCE_BIG, false,
                PropertyAccessor.of(
                        BTETerraRendererConfig.UI::getSidebarWidth,
                        BTETerraRendererConfig.UI::setSidebarWidth
                ),
                PropertyAccessor.of(SidebarSide.class,
                        BTETerraRendererConfig.UI::getSidebarSide,
                        BTETerraRendererConfig.UI::setSidebarSide
                )
        );

        // Don't make sidebar elements here if they need i18n keys. Keys won't get updated.
        // instead put all of those elements in getElements()

        this.yAxisInputWrapper = new SidebarElementWrapper();
        this.mapSourceDropdown = new SidebarDropdownSelector<>(
                PropertyAccessor.of(BTRUtil.uncheckedCast(CategoryMap.Wrapper.class),
                        this::getWrappedTMS, this::setTileMapServiceWrapper),
                MapRenderingOptionsSidebar::tmsWrappedToString,
                MapRenderingOptionsSidebar::getIconTextureObject
        );
        this.mapCopyright = new SidebarTextComponent(HorizontalAlign.LEFT);
        this.tmsPropertyElementList = new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false);
    }

    @Override
    protected List<GuiSidebarElement> getElements() {
        BTETerraRendererConfig.HologramConfig hologramSettings = BTETerraRendererConfig.HOLOGRAM;

        SidebarBooleanButton renderingTrigger = new SidebarBooleanButton(
                PropertyAccessor.of(hologramSettings::isDoRender, hologramSettings::setDoRender),
                I18nManager.format("gui.bteterrarenderer.settings.map_rendering") + ": "
        );
        SidebarSlider<Double> opacitySlider = new SidebarSlider<>(
                RangedDoublePropertyAccessor.of(hologramSettings::getOpacity, hologramSettings::setOpacity, 0, 1),
                I18nManager.format("gui.bteterrarenderer.settings.opacity") + ": ", ""
        );
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
                PropertyAccessor.of(hologramSettings::getXAlign, hologramSettings::setXAlign),
                PropertyAccessor.of(hologramSettings::getZAlign, hologramSettings::setZAlign),
                PropertyAccessor.of(hologramSettings::isLockNorth, hologramSettings::setLockNorth)
        );

        SidebarHorizontalLine hl = new SidebarHorizontalLine(1, 0xFFFFFFFF);

        return Arrays.asList(
            // ===========================================================================================
            new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false).addAll(
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.title"), HorizontalAlign.CENTER, 0xFFFFFFFF)
            ),

            // General components
            new SidebarElementList(ELEMENT_DISTANCE, 0, null, false).addAll(
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.general"), HorizontalAlign.LEFT, 0xFFFFFFFF),
                hl, // ---------------------------------------------------------------------------------------
                new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false).addAll(
                    renderingTrigger,
                    opacitySlider,
                    this.yAxisInputWrapper
                )
            ),

            // Map source control components
            new SidebarElementList(ELEMENT_DISTANCE, 0, null, false).addAll(
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_source"), HorizontalAlign.LEFT, 0xFFFFFFFF),
                hl, // ---------------------------------------------------------------------------------------
                new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false).addAll(
                    this.mapSourceDropdown,
                    this.mapCopyright
                )
            ),

            this.tmsPropertyElementList,

            new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false).addAll(
                openMapsFolderButton,
                reloadMapsButton
            ),

            // Map offset control components
            new SidebarElementList(ELEMENT_DISTANCE, 0, null, false).addAll(
                new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_offset"), HorizontalAlign.LEFT, 0xFFFFFFFF),
                hl, // ---------------------------------------------------------------------------------------
                new SidebarElementList(ELEMENT_DISTANCE, ELEMENT_DISTANCE, null, false).addAll(
                    mapAligner
                )
            )
            // ===========================================================================================
        );
    }

    @Override
    protected void drawScreen(Object poseStack) {
        ICON_MAKER.iconBaker.process(1);
        super.drawScreen(poseStack);
    }

    private CategoryMap.Wrapper<TileMapService<?>> getWrappedTMS() {
        return BTETerraRendererConfig.getTileMapServiceWrapper();
    }

    private void setTileMapServiceWrapper(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HOLOGRAM;

        // Check null
        @SuppressWarnings("resource")
        TileMapService<?> tms = Optional.ofNullable(tmsWrapped).map(CategoryMap.Wrapper::getItem).orElse(null);
        if(tms == null) {
            this.yAxisInputWrapper.hide = true;
            this.tmsPropertyElementList.hide = true;
            this.mapCopyright.hide = true;
            return;
        }

        // Set to config
        BTETerraRendererConfig.setTileMapService(tmsWrapped);

        // Set property element list
        SidebarText text = new SidebarText(I18nManager.format("gui.bteterrarenderer.settings.map_settings"), HorizontalAlign.LEFT);
        this.tmsPropertyElementList.clear();
        this.tmsPropertyElementList.add(text);
        this.tmsPropertyElementList.addProperties(tms.getProperties());
        this.tmsPropertyElementList.hide = false;

        // Set y axis input
        this.yAxisInputWrapper.hide = false;
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
        String textComponentJson = Optional.ofNullable(tms.getCopyrightTextJson())
                .map(Translatable::get)
                .orElse(null);
        this.mapCopyright.hide = textComponentJson == null;
        this.mapCopyright.setTextComponentJson(textComponentJson);
    }

    private static String tmsWrappedToString(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped) {
        TileMapService<?> tms = tmsWrapped.getItem();
        if(tms == null) {
            return "[§7" + tmsWrapped.getSource() + "§r]\n§4§o(error)";
        }

        String name = tms.getName().get();
        if("default".equalsIgnoreCase(tmsWrapped.getSource())) {
            return name;
        }
        return "[§7" + tmsWrapped.getSource() + "§r]\n§r" + name;
    }

    private void reloadMapSources() {
        BTETerraRendererConfig.load(true);
        this.updateMapSourceDropdown();
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
                Loggers.sendErrorMessageToChat("Cannot open file explorer! Instead you can manually go to:");
                Loggers.sendErrorMessageToChat(directory.getAbsolutePath());
                return;
            }
            p.waitFor(3, TimeUnit.SECONDS);
            p.destroy();
        } catch(Exception e) {
            Loggers.sendErrorMessageToChat(this, "Error opening the map folder!", e);
        }
    }

    private static Object getIconTextureObject(CategoryMap.Wrapper<TileMapService<?>> wrapper) {
        TileMapService<?> tms = wrapper.getItem();
        if(tms == null) return null;

        URL iconUrl = tms.getIconUrl();
        if(iconUrl == null) return null;
        return ICON_MAKER.updateOrInsert(iconUrl, () -> iconUrl);
    }

    public static void open() {
        if(INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        BTETerraRendererConfig.save();
        INSTANCE.updateMapSourceDropdown();
        INSTANCE.setTileMapServiceWrapper(BTETerraRendererConfig.getTileMapServiceWrapper());
        RawGuiManager.INSTANCE.displayGuiScreen(INSTANCE);
    }

    @Override
    public void onClose() {
        BTETerraRendererConfig.save();
        super.onClose();
    }

    private static class IconMaker extends CacheableProcessorModel<URL, URL, Object> {

        private final SimpleImageFetchingBlock<URL> iconFetcher = new SimpleImageFetchingBlock<>(
                Executors.newCachedThreadPool(), 3, 500);
        private final ImageResizingBlock<URL> imageResize = new ImageResizingBlock<>(256, 256);
        private final ImageBakingBlock<URL> iconBaker = new ImageBakingBlock<>();

        protected IconMaker() {
            super(new ProcessorCacheStorage<>(-1, -1, false));
        }

        @Override
        protected SequentialBuilder<URL, URL, Object> getSequentialBuilder() {
            return new SequentialBuilder<>(this.iconFetcher)
                    .then(this.imageResize)
                    .then(this.iconBaker);
        }

        @Override
        protected void deleteResource(Object o) {
            GlGraphicsManager.INSTANCE.deleteTextureObject(o);
        }
    }
}
