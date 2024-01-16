package com.mndk.bteterrarenderer.core.loader.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMapServicePropertyLoader {

    private static final String FILE_NAME = "properties.json";
    private static final TypeReference<CategoryMap<Map<String, Object>>> TYPE_REF =
            new TypeReference<CategoryMap<Map<String, Object>>>() {};

    public static void load(CategoryMap<TileMapService<?>> tmsCategoryMap) {
        if(BTETerraRendererConfig.getModConfigDirectory() == null) {
            Loggers.get().error("Mod config file is null");
            return;
        }
        File file = new File(BTETerraRendererConfig.getModConfigDirectory(), FILE_NAME);
        if(!file.isFile()) return;

        CategoryMap<Map<String, Object>> raw;
        try {
            raw = BTETerraRendererConstants.JSON_MAPPER.readValue(file, TYPE_REF);
        } catch (IOException e) {
            Loggers.get().error("Cannot read TMS property json file", e);
            return;
        }

        for(CategoryMap.Wrapper<Map<String, Object>> wrappedMap : raw.getItemWrappers()) {
            String categoryName = wrappedMap.getParentCategory().getName(), id = wrappedMap.getId();
            TileMapService<?> tms = tmsCategoryMap.getItem(categoryName, id);
            if(tms == null) continue;

            List<PropertyAccessor.Localized<?>> tmsProperties = tms.getProperties();
            Map<String, Object> propertyValues = wrappedMap.getItem();
            for(PropertyAccessor.Localized<?> tmsProperty : tmsProperties) {
                String key = tmsProperty.key;
                if(!propertyValues.containsKey(key)) continue;
                try {
                    tmsProperty.delegate.set(BTRUtil.uncheckedCast(propertyValues.get(key)));
                } catch(Exception e) {
                    Loggers.get().error(e);
                }
            }
        }
    }

    public static void save(@Nullable CategoryMap<TileMapService<?>> tmsCategoryMap) {
        if(BTETerraRendererConfig.getModConfigDirectory() == null) return;
        if(tmsCategoryMap == null) return;

        CategoryMap<Map<String, Object>> raw = new CategoryMap<>();
        for(CategoryMap.Wrapper<TileMapService<?>> tmsWrapped : tmsCategoryMap.getItemWrappers()) {
            Map<String, Object> propertyValues = new HashMap<>();

            TileMapService<?> tms = tmsWrapped.getItem();
            for(PropertyAccessor.Localized<?> tmsProperty : tms.getProperties()) {
                propertyValues.put(tmsProperty.key, tmsProperty.delegate.get());
            }
            raw.setItem(tmsWrapped.getParentCategory().getName(), tmsWrapped.getId(), propertyValues);
        }

        try {
            File file = new File(BTETerraRendererConfig.getModConfigDirectory(), FILE_NAME);
            BTETerraRendererConstants.JSON_MAPPER.writeValue(file, raw);
        } catch(IOException e) {
            Loggers.get().error("Cannot write TMS property json file", e);
        }
    }
}
