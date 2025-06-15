package com.mndk.bteterrarenderer.core.loader.yml;

import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.category.CategoryMap;
import com.mndk.bteterrarenderer.util.loader.YamlLoader;
import com.mndk.bteterrarenderer.util.merge.CategoryMapMergeStrategy;

public class TileMapServiceLoader extends YamlLoader<TileMapServiceDTO, CategoryMap<TileMapService>> {

    public TileMapServiceLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath, TileMapServiceDTO.class, new CategoryMapMergeStrategy<>());
    }

    @Override
    public void refresh() {
        if (result != null) result.forEach((cn, category) -> category.forEach((n, tms) -> {
            try { tms.close(); }
            catch (Exception e) { Loggers.get(this).error("Couldn't close TMS", e); }
        }));
        super.refresh();
    }

    protected CategoryMap<TileMapService> load(String fileName, TileMapServiceDTO content) {
        CategoryMap<TileMapService> categoryMap = content.getCategories();
        categoryMap.forEach((categoryName, category) ->
                category.forEach((key, tms) -> tms.setSource(fileName))
        );
        return categoryMap;
    }
}
