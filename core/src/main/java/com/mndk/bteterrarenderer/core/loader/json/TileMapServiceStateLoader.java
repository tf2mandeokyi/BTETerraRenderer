package com.mndk.bteterrarenderer.core.loader.json;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.util.category.CategoryMap;
import com.mndk.bteterrarenderer.util.loader.ConfigLoader;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TileMapServiceStateLoader implements ConfigLoader<CategoryMap<TileMapService>> {

    private final File file;

    @Override
    public void load(CategoryMap<TileMapService> tmsCategoryMap) {
        try {
            TileMapServicePropertyDTO raw = BTETerraRenderer.JSON_MAPPER.readValue(this.file, TileMapServicePropertyDTO.class);
            this.applyRawFileData(tmsCategoryMap, raw);
        } catch (FileNotFoundException e) {
            Loggers.get(this).warn("TMS property json file not found, skipping");
        } catch (IOException e) {
            Loggers.get(this).error("Cannot read TMS property json file", e);
        }
    }

    private void applyRawFileData(@Nonnull CategoryMap<TileMapService> tmsCategoryMap, TileMapServicePropertyDTO raw) {
        raw.getCategories().forEach((categoryName, category) -> category.forEach((id, map) -> {
            TileMapService tms = tmsCategoryMap.getItem(categoryName, id);
            if (tms == null || map == null) return; // skip if TMS not found
            this.applyRawStates(tms, map);
        }));
    }

    private void applyRawStates(@Nonnull TileMapService tms, Map<String, Object> rawValues) {
        List<PropertyAccessor.Localized<?>> states = tms.getStateAccessors();
        for (PropertyAccessor.Localized<?> state : states) {
            String key = state.getKey();
            if (!rawValues.containsKey(key)) continue;
            try { state.set(BTRUtil.uncheckedCast(rawValues.get(key))); }
            catch (Exception e) { Loggers.get(this).error("Could not set property for TMS", e); }
        }
    }

    @Override
    public void save(@Nullable CategoryMap<TileMapService> tmsCategoryMap) {
        if (tmsCategoryMap == null) return;

        try {
            CategoryMap<Map<String, Object>> map = this.prepareRawFileData(tmsCategoryMap);
            TileMapServicePropertyDTO content = new TileMapServicePropertyDTO(map);
            BTETerraRenderer.JSON_MAPPER.writeValue(this.file, content);
        } catch (IOException e) {
            Loggers.get(this).error("Cannot write TMS property json file", e);
        }
    }

    private CategoryMap<Map<String, Object>> prepareRawFileData(@Nonnull CategoryMap<TileMapService> tmsCategoryMap) {
        CategoryMap<Map<String, Object>> map = new CategoryMap<>();
        tmsCategoryMap.forEach((categoryName, category) -> category.forEach((id, tms) -> {
            Map<String, Object> propertyValues = new HashMap<>();
            this.saveRawStates(tms, propertyValues);
            map.setItem(categoryName, id, propertyValues);
        }));
        return map;
    }

    private void saveRawStates(@Nonnull TileMapService tms, Map<String, Object> rawValues) {
        List<PropertyAccessor.Localized<?>> states = tms.getStateAccessors();
        for (PropertyAccessor.Localized<?> state : states) {
            String key = state.getKey();
            rawValues.put(key, state.get());
        }
    }
}
