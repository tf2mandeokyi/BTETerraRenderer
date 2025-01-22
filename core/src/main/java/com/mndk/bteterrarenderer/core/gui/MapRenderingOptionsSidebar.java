package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.ImageBakingBlock;
import com.mndk.bteterrarenderer.core.graphics.ImageResizingBlock;
import com.mndk.bteterrarenderer.core.gui.mapaligner.MapAligner;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.network.SimpleImageFetchingBlock;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFX;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXBooleanButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown.McFXDropdown;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.slider.McFXSlider;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXWrapper;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;
    private static final int SIDE_PADDING = 7;
    private static final int ELEMENT_DISTANCE_BIG = 35;

    private static final IconMaker ICON_MAKER = new IconMaker();

    private static MapRenderingOptionsSidebar INSTANCE;
    private final McFXDropdown<CategoryMap.Wrapper<TileMapService>> mapSourceDropdown;
    private final McFXElement mapCopyright;
    private final McFXVerticalList tmsStateElementList;
    private final McFXWrapper yAxisInputWrapper;

    private MapRenderingOptionsSidebar() {
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
        // Instead, put all those elements in getSidebarElements()

        this.yAxisInputWrapper = McFX.wrapper();
        this.mapSourceDropdown = McFX.dropdown(
                PropertyAccessor.of(BTRUtil.uncheckedCast(CategoryMap.Wrapper.class),
                        this::getWrappedTMS, this::setTileMapServiceWrapper),
                MapRenderingOptionsSidebar::tmsWrappedToString,
                MapRenderingOptionsSidebar::getIconTextureObject
        );
        this.mapCopyright = McFX.div().setAlign(HorizontalAlign.LEFT);
        this.tmsStateElementList = McFX.vList(ELEMENT_DISTANCE, ELEMENT_DISTANCE);
    }

    @Override
    protected List<McFXElement> getSidebarElements() {
        BTETerraRendererConfig.HologramConfig hologramSettings = BTETerraRendererConfig.HOLOGRAM;

        McFXBooleanButton renderingTrigger = McFX.i18nBoolButton(
                "gui.bteterrarenderer.settings.map_rendering",
                PropertyAccessor.of(hologramSettings::isDoRender, hologramSettings::setDoRender)
        );
        McFXSlider<Double> opacitySlider = McFX.i18nSlider(
                "gui.bteterrarenderer.settings.opacity",
                PropertyAccessor.ranged(hologramSettings::getOpacity, hologramSettings::setOpacity, 0, 1)
        );
        McFXButton reloadMapsButton = McFX.i18nButton(
                "gui.bteterrarenderer.settings.map_reload",
                (self, mouseButton) -> this.reloadMapSources()
        );
        McFXButton openMapsFolderButton = McFX.i18nButton(
                "gui.bteterrarenderer.settings.map_folder",
                (self, mouseButton) -> this.openMapsFolder()
        );

        // Map orientation components
        MapAligner mapAligner = new MapAligner(
                PropertyAccessor.of(hologramSettings::getXAlign, hologramSettings::setXAlign),
                PropertyAccessor.of(hologramSettings::getZAlign, hologramSettings::setZAlign),
                PropertyAccessor.of(hologramSettings::isLockNorth, hologramSettings::setLockNorth)
        );

        McFXElement hl = McFX.div(1).setBackgroundColor(0xFFFFFFFF);

        return Arrays.asList(
                // ===========================================================================================
                McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING)
                        .add(McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.title")
                                .setAlign(HorizontalAlign.CENTER)
                                .setColor(0xFFFFFFFF)),

                // General components
                McFX.vList(ELEMENT_DISTANCE, 0)
                        .add(McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.general")
                                .setAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF))
                        .add(hl) // --------------------------------------------------------------------------
                        .add(McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING)
                                .add(renderingTrigger)
                                .add(opacitySlider)
                                .add(this.yAxisInputWrapper)),

                // Map source control components
                McFX.vList(ELEMENT_DISTANCE, 0)
                        .add(McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.map_source")
                                .setAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF))
                        .add(hl) // --------------------------------------------------------------------------
                        .add(McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING)
                                .add(this.mapSourceDropdown)
                                .add(this.mapCopyright)),

                this.tmsStateElementList,

                McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING)
                        .add(openMapsFolderButton)
                        .add(reloadMapsButton),

                // Map offset control components
                McFX.vList(ELEMENT_DISTANCE, 0)
                        .add(McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.map_offset")
                                .setAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF))
                        .add(hl) // --------------------------------------------------------------------------
                        .add(McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING)
                                .add(mapAligner))
                // ===========================================================================================
        );
    }

    @Override
    public void drawScreen(@Nonnull GuiDrawContextWrapper drawContextWrapper) {
        ICON_MAKER.iconBaker.process(1);
        super.drawScreen(drawContextWrapper);
    }

    private CategoryMap.Wrapper<TileMapService> getWrappedTMS() {
        return TileMapService.getSelected();
    }

    private void setTileMapServiceWrapper(CategoryMap.Wrapper<TileMapService> tmsWrapped) {
        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HOLOGRAM;

        // Check null
        TileMapService tms = tmsWrapped != null ? tmsWrapped.getItem() : null;
        if (tms == null) {
            this.yAxisInputWrapper.hide = true;
            this.tmsStateElementList.hide = true;
            this.mapCopyright.hide = true;
            return;
        }

        // Set to config
        TileMapService.selectForDisplay(tmsWrapped);

        // Set property element list
        this.tmsStateElementList.clear()
                .add(McFX.div()
                        .setI18nKeyContent("gui.bteterrarenderer.settings.map_settings")
                        .setAlign(HorizontalAlign.LEFT))
                .addProperties(tms.getStateAccessors());
        this.tmsStateElementList.hide = false;

        // Set y axis input
        this.yAxisInputWrapper.hide = false;
        if (tms instanceof FlatTileMapService) {
            this.yAxisInputWrapper.setElement(McFX.i18nNumberInput(
                    "gui.bteterrarenderer.settings.map_y_level",
                    PropertyAccessor.of(renderSettings::getFlatMapYAxis, renderSettings::setFlatMapYAxis))
            );
        } else {
            this.yAxisInputWrapper.setElement(McFX.i18nNumberInput(
                    "gui.bteterrarenderer.settings.y_align",
                    PropertyAccessor.of(renderSettings::getYAlign, renderSettings::setYAlign))
            );
        }

        // Set copyright
        String textComponentJson = Optional.ofNullable(tms.getCopyrightTextJson())
                .map(Translatable::get)
                .orElse(null);
        this.mapCopyright.hide = textComponentJson == null;
        this.mapCopyright.setTextJsonContent(textComponentJson);
    }

    private static String tmsWrappedToString(CategoryMap.Wrapper<TileMapService> tmsWrapped) {
        TileMapService tms = tmsWrapped.getItem();
        if (tms == null) {
            return "[§7" + tmsWrapped.getSource() + "§r]\n§4§o(error)";
        }

        String name = tms.getName().get();
        if ("default".equalsIgnoreCase(tmsWrapped.getSource())) {
            return name;
        }
        return "[§7" + tmsWrapped.getSource() + "§r]\n§r" + name;
    }

    private void reloadMapSources() {
        BTETerraRendererConfig.load(true);
        this.updateMapSourceDropdown();
    }

    private void updateMapSourceDropdown() {
        McFXDropdown<CategoryMap.Wrapper<TileMapService>>.ItemListUpdater updater =
                mapSourceDropdown.itemListBuilder();

        CategoryMap<TileMapService> tmsCategoryMap = ConfigLoaders.tms().getResult();
        tmsCategoryMap.getCategories().forEach(categoryEntry -> {
            CategoryMap.Category<TileMapService> category = categoryEntry.getValue();
            updater.push(categoryEntry.getKey());
            category.values().forEach(updater::add);
            updater.pop();
        });
        updater.update();
    }

    private void openMapsFolder() {
        try {
            File directory = ConfigLoaders.tms().getFilesDirectory();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory);
                return;
            }

            Process p;
            if (System.getProperty("os.name").startsWith("Windows")) {
                p = new ProcessBuilder("explorer.exe", "/select," + directory.getAbsolutePath()).start();
            }
            else if (System.getProperty("os.name").startsWith("Mac")) {
                p = new ProcessBuilder("usr/bin/open", directory.getAbsolutePath()).start();
            }
            else {
                McConnector.sendErrorMessageToChat("Cannot open file explorer! Instead you can manually go to:");
                McConnector.sendErrorMessageToChat(directory.getAbsolutePath());
                return;
            }
            p.waitFor(3, TimeUnit.SECONDS);
            p.destroy();
        } catch (Exception e) {
            McConnector.sendErrorMessageToChat(this, "Error opening the map folder!", e);
        }
    }

    private static NativeTextureWrapper getIconTextureObject(CategoryMap.Wrapper<TileMapService> wrapper) {
        TileMapService tms = wrapper.getItem();
        if (tms == null) return null;

        URL iconUrl = tms.getIconUrl();
        if (iconUrl == null) return null;
        return ICON_MAKER.updateOrInsert(iconUrl, iconUrl);
    }

    public static void open() {
        if (INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        BTETerraRendererConfig.save();
        INSTANCE.updateMapSourceDropdown();
        INSTANCE.setTileMapServiceWrapper(TileMapService.getSelected());
        McConnector.client().displayGuiScreen(INSTANCE);
    }

    @Override
    public void onRemoved() {
        BTETerraRendererConfig.save();
        super.onRemoved();
    }

    private static class IconMaker extends CacheableProcessorModel<URL, URL, NativeTextureWrapper> {

        private final SimpleImageFetchingBlock<URL> iconFetcher = new SimpleImageFetchingBlock<>(
                Executors.newCachedThreadPool(), 3, 500, true);
        private final ImageResizingBlock<URL> imageResize = new ImageResizingBlock<>(256, 256);
        private final ImageBakingBlock<URL> iconBaker = new ImageBakingBlock<>();

        protected IconMaker() {
            super(new ProcessorCacheStorage<>(-1, -1, false));
        }

        @Override
        protected SequentialBuilder<URL, URL, NativeTextureWrapper> getSequentialBuilder() {
            return new SequentialBuilder<>(this.iconFetcher)
                    .then(this.imageResize)
                    .then(this.iconBaker);
        }

        @Override
        protected void deleteResource(NativeTextureWrapper o) {
            McConnector.client().glGraphicsManager.deleteTextureObject(o);
        }
    }
}
