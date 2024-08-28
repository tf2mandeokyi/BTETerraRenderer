package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import lombok.experimental.UtilityClass;

@UtilityClass
class TileMapServiceSelection {

    private CategoryMap.Wrapper<TileMapService> ON_DISPLAY;

    CategoryMap.Wrapper<TileMapService> get() {
        return ON_DISPLAY;
    }

    void set(CategoryMap.Wrapper<TileMapService> wrapped) {
        ON_DISPLAY = wrapped;
        BTETerraRendererConfig.GENERAL.setMapServiceCategory(wrapped.getParentCategory().getName());
        BTETerraRendererConfig.GENERAL.setMapServiceId(wrapped.getId());
    }

    void refresh() {
        String category = BTETerraRendererConfig.GENERAL.getMapServiceCategory();
        String id = BTETerraRendererConfig.GENERAL.getMapServiceId();
        ON_DISPLAY = ConfigLoaders.tms().getResult().getItemWrapper(category, id);
    }

}
