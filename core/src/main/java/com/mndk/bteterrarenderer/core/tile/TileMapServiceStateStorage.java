package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import lombok.experimental.UtilityClass;

@UtilityClass
class TileMapServiceStateStorage {

    CategoryMap.Wrapper<TileMapService> TMS_ON_DISPLAY;

    CategoryMap.Wrapper<TileMapService> getCurrentWrapped() {
        return TMS_ON_DISPLAY;
    }

    void setCurrentWrapped(CategoryMap.Wrapper<TileMapService> wrapped) {
        TMS_ON_DISPLAY = wrapped;
        BTETerraRendererConfig.GENERAL.setMapServiceCategory(wrapped.getParentCategory().getName());
        BTETerraRendererConfig.GENERAL.setMapServiceId(wrapped.getId());
    }

    void refreshCurrentTileMapService() {
        String category = BTETerraRendererConfig.GENERAL.getMapServiceCategory();
        String id = BTETerraRendererConfig.GENERAL.getMapServiceId();
        TMS_ON_DISPLAY = ConfigLoaders.tms().getResult().getItemWrapper(category, id);
    }

}
