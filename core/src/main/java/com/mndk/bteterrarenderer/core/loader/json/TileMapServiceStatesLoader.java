package com.mndk.bteterrarenderer.core.loader.json;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
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
public class TileMapServiceStatesLoader {

    private final File file;

    public void load(CategoryMap<TileMapService> tmsCategoryMap) {
        try {
            TileMapServicePropertyJsonFile raw = BTETerraRenderer.JSON_MAPPER.readValue(this.file, TileMapServicePropertyJsonFile.class);
            this.applyRawFileData(tmsCategoryMap, raw);
        } catch (FileNotFoundException e) {
            Loggers.get(this).warn("TMS property json file not found, skipping");
        } catch (IOException e) {
            Loggers.get(this).error("Cannot read TMS property json file", e);
        }
    }

    private void applyRawFileData(@Nonnull CategoryMap<TileMapService> tmsCategoryMap, TileMapServicePropertyJsonFile raw) {
        for (CategoryMap.Wrapper<Map<String, Object>> wrappedMap : raw.getCategories().getItemWrappers()) {
            String categoryName = wrappedMap.getParentCategory().getName(), id = wrappedMap.getId();
            TileMapService tms = tmsCategoryMap.getItem(categoryName, id);
            if (tms == null) continue;
            Map<String, Object> propertyValues = wrappedMap.getItem();
            this.applyRawStates(tms, propertyValues);
        }
    }

    private void applyRawStates(@Nonnull TileMapService tms, Map<String, Object> rawValues) {
        List<PropertyAccessor.Localized<?>> states = tms.getStateAccessors();
        for (PropertyAccessor.Localized<?> state : states) {
            String key = state.getKey();
            if (!rawValues.containsKey(key)) continue;
            try {
                state.set(BTRUtil.uncheckedCast(rawValues.get(key)));
            } catch (Exception e) {
                Loggers.get(this).error("Could not set property for TMS", e);
            }
        }
    }

    public void save(@Nullable CategoryMap<TileMapService> tmsCategoryMap) {
        if (tmsCategoryMap == null) return;

        try {
            CategoryMap<Map<String, Object>> map = this.prepareRawFileData(tmsCategoryMap);
            TileMapServicePropertyJsonFile content = new TileMapServicePropertyJsonFile(map);
            BTETerraRenderer.JSON_MAPPER.writeValue(this.file, content);
        } catch (IOException e) {
            Loggers.get(this).error("Cannot write TMS property json file", e);
        }
    }

    private CategoryMap<Map<String, Object>> prepareRawFileData(@Nonnull CategoryMap<TileMapService> tmsCategoryMap) {
        CategoryMap<Map<String, Object>> map = new CategoryMap<>();
        for (CategoryMap.Wrapper<TileMapService> tmsWrapped : tmsCategoryMap.getItemWrappers()) {
            Map<String, Object> propertyValues = new HashMap<>();
            TileMapService tms = tmsWrapped.getItem();
            this.saveRawStates(tms, propertyValues);
            map.setItem(tmsWrapped.getParentCategory().getName(), tmsWrapped.getId(), propertyValues);
        }
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
