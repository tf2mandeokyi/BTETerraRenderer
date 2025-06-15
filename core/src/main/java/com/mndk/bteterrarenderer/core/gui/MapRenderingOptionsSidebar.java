package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.util.concurrent.ManualThreadExecutor;
import com.mndk.bteterrarenderer.core.gui.mapaligner.MapAligner;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.core.loader.LoaderRegistry;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.util.category.CategoryMap;
import com.mndk.bteterrarenderer.util.image.ImageUtil;
import com.mndk.bteterrarenderer.util.concurrent.CacheStorage;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapRenderingOptionsSidebar extends GuiSidebar {

    private static final int ELEMENT_DISTANCE = 7;
    private static final int SIDE_PADDING = 7;
    private static final int ELEMENT_DISTANCE_BIG = 35;

    private static final ExecutorService MULTI_THREADED = Executors.newCachedThreadPool();
    private static final ManualThreadExecutor ICON_MAKER = new ManualThreadExecutor();
    private static final IconStorage ICON_STORAGE = new IconStorage();

    private static MapRenderingOptionsSidebar INSTANCE;
    private final McFXDropdown mapSourceDropdown;
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
                PropertyAccessor.of(BTRUtil.uncheckedCast(String[].class),
                        this::getWrappedTMS, this::setTileMapServiceCategoryPath),
                MapRenderingOptionsSidebar::tmsWrappedToString,
                MapRenderingOptionsSidebar::getIconTextureObject
        );
        this.mapCopyright = McFX.div().setHorizontalAlign(HorizontalAlign.LEFT);
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
        this.updateTmsSettings();

        return Arrays.asList(
                // ===========================================================================================
                McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING).addAll(
                        McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.title")
                                .setHorizontalAlign(HorizontalAlign.CENTER)
                                .setColor(0xFFFFFFFF)
                ),

                // General components
                McFX.vList(ELEMENT_DISTANCE, 0).addAll(
                        McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.general")
                                .setHorizontalAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF),
                        hl, // --------------------------------------------------------------------------
                        McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING).addAll(
                                renderingTrigger,
                                opacitySlider,
                                this.yAxisInputWrapper
                        )
                ),

                // Map source control components
                McFX.vList(ELEMENT_DISTANCE, 0).addAll(
                        McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.map_source")
                                .setHorizontalAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF),
                        hl, // --------------------------------------------------------------------------
                        McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING).addAll(
                                this.mapSourceDropdown,
                                this.mapCopyright
                        )
                ),

                this.tmsStateElementList,

                McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING).addAll(
                        openMapsFolderButton,
                        reloadMapsButton
                ),

                // Map offset control components
                McFX.vList(ELEMENT_DISTANCE, 0).addAll(
                        McFX.div()
                                .setI18nKeyContent("gui.bteterrarenderer.settings.map_offset")
                                .setHorizontalAlign(HorizontalAlign.LEFT)
                                .setColor(0xFFFFFFFF),
                        hl, // --------------------------------------------------------------------------
                        McFX.vList(ELEMENT_DISTANCE, SIDE_PADDING).addAll(
                                mapAligner
                        )
                )
                // ===========================================================================================
        );
    }

    @Override
    public void drawScreen(@Nonnull GuiDrawContextWrapper drawContextWrapper) {
        ICON_MAKER.process(1);
        super.drawScreen(drawContextWrapper);
    }

    private String[] getWrappedTMS() {
        return new String[] {
                BTETerraRendererConfig.GENERAL.getMapServiceCategory(),
                BTETerraRendererConfig.GENERAL.getMapServiceId()
        };
    }

    private void updateTmsSettings() {
        BTETerraRendererConfig.HologramConfig renderSettings = BTETerraRendererConfig.HOLOGRAM;
        TileMapService tms = LoaderRegistry.getCurrentTMS();
        if (tms == null) {
            this.yAxisInputWrapper.hide = true;
            this.tmsStateElementList.hide = true;
            this.mapCopyright.hide = true;
            return;
        }

        // Set property element list
        this.tmsStateElementList.clear().addAll(
                McFX.div()
                        .setI18nKeyContent("gui.bteterrarenderer.settings.map_settings")
                        .setHorizontalAlign(HorizontalAlign.LEFT)
                )
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

    private void setTileMapServiceCategoryPath(String[] categoryPath) {
        if (categoryPath == null || categoryPath.length != 2) {
            throw new IllegalArgumentException("Category stack must have exactly 3 elements: "
                    + "[categoryName, id]. Current: "
                    + Arrays.toString(categoryPath));
        }
        String categoryName = categoryPath[0];
        String id = categoryPath[1];
        BTETerraRendererConfig.GENERAL.setMapServiceCategory(categoryName);
        BTETerraRendererConfig.GENERAL.setMapServiceId(id);
        this.updateTmsSettings();
    }

    private static String tmsWrappedToString(String[] categoryPath) {
        if (categoryPath == null || categoryPath.length != 2) {
            throw new IllegalArgumentException("Category stack must have exactly 3 elements: "
                    + "[categoryName, id]. Current: "
                    + Arrays.toString(categoryPath));
        }
        String categoryName = categoryPath[0];
        String id = categoryPath[1];
        TileMapService tms = LoaderRegistry.tms().getResult().getItem(categoryName, id);
        if (tms == null) {
            return "[§7null§r]\n§4§o(error)";
        }

        String name = tms.getName().get();
        if ("default".equalsIgnoreCase(tms.getSource())) {
            return name;
        }
        return "[§7" + tms.getSource() + "§r]\n§r" + name;
    }

    private void reloadMapSources() {
        LoaderRegistry.save();
        LoaderRegistry.load(true);
        this.updateMapSourceDropdown();
        this.updateTmsSettings();
    }

    private void updateMapSourceDropdown() {
        McFXDropdown.ItemListUpdater updater = mapSourceDropdown.itemListBuilder();

        CategoryMap<TileMapService> tmsCategoryMap = LoaderRegistry.tms().getResult();
        tmsCategoryMap.forEach((name, category) -> {
            updater.push(name);
            category.forEach((id, tms) -> updater.add(id));
            updater.pop();
        });
        updater.update();
    }

    private void openMapsFolder() {
        try {
            File directory = LoaderRegistry.tms().getFilesDirectory();
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

    private static NativeTextureWrapper getIconTextureObject(String[] categoryPath) {
        if (categoryPath == null || categoryPath.length != 2) {
            throw new IllegalArgumentException("Category stack must have exactly 3 elements: "
                    + "[categoryName, id]. Current: "
                    + Arrays.toString(categoryPath));
        }
        String categoryName = categoryPath[0];
        String id = categoryPath[1];
        TileMapService tms = LoaderRegistry.tms().getResult().getItem(categoryName, id);
        if (tms == null) return null;

        URL iconUrl = tms.getIconUrl();
        if (iconUrl == null) return null;

        return ICON_STORAGE.getOrCompute(iconUrl, () -> HttpResourceManager.downloadAsImage(iconUrl.toString(), null)
                .thenApplyAsync(
                        image -> ImageUtil.resizeImage(image, 256, 256),
                        MULTI_THREADED
                )
                .thenApplyAsync(
                        image -> McConnector.client().textureManager.allocateAndGetTextureObject(BTETerraRenderer.MODID, image),
                        ICON_MAKER
                ));
    }

    public static void open() {
        if (INSTANCE == null) INSTANCE = new MapRenderingOptionsSidebar();
        LoaderRegistry.save();
        INSTANCE.updateMapSourceDropdown();
        McConnector.client().displayGuiScreen(INSTANCE);
    }

    @Override
    public void onRemoved() {
        LoaderRegistry.save();
        super.onRemoved();
    }

    private static class IconStorage extends CacheStorage<URL, NativeTextureWrapper> {
        protected IconStorage() { super(-1, -1, false); }

        @Override
        protected void delete(NativeTextureWrapper value) {
            McConnector.client().textureManager.deleteTextureObject(value);
        }
    }
}
