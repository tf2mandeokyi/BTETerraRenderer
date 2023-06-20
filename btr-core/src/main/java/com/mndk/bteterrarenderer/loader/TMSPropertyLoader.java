package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.TileMapService;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.PropertyAccessor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMSPropertyLoader {
    private static final String FILE_NAME = "properties.json";

    public static void load(CategoryMap<TileMapService> tmsCategoryMap) throws IOException {
        if(ConfigLoaders.MOD_CONFIG_DIRECTORY == null) {
            BTETerraRendererConstants.LOGGER.warn("Mod config file is null");
            return;
        }
        File file = new File(ConfigLoaders.MOD_CONFIG_DIRECTORY, FILE_NAME);
        if(!file.isFile()) return;

        CategoryMap<Map<String, Object>> raw = BTETerraRendererConstants.JSON_MAPPER.readValue(
                file, new TypeReference<CategoryMap<Map<String, Object>>>() {});

        for(CategoryMap.Wrapper<Map<String, Object>> wrappedMap : raw.getItemWrappers()) {
            String categoryName = wrappedMap.getParentCategory().getName(), id = wrappedMap.getId();
            TileMapService tms = tmsCategoryMap.getItem(categoryName, id);
            if(tms == null) continue;

            List<PropertyAccessor.Localized<?>> tmsProperties = tms.getProperties();
            Map<String, Object> propertyValues = wrappedMap.getItem();
            for(PropertyAccessor.Localized<?> tmsProperty : tmsProperties) {
                String key = tmsProperty.key;
                if(!propertyValues.containsKey(key)) continue;
                try {
                    tmsProperty.delegate.set(BtrUtil.uncheckedCast(propertyValues.get(key)));
                } catch(Exception e) {
                    BTETerraRendererConstants.LOGGER.info(e);
                }
            }
        }
    }

    public static void save(@Nullable CategoryMap<TileMapService> tmsCategoryMap) throws IOException {
        if(ConfigLoaders.MOD_CONFIG_DIRECTORY == null) return;
        if(tmsCategoryMap == null) return;

        CategoryMap<Map<String, Object>> raw = new CategoryMap<>();
        for(CategoryMap.Wrapper<TileMapService> tmsWrapped : tmsCategoryMap.getItemWrappers()) {
            Map<String, Object> propertyValues = new HashMap<>();

            TileMapService tms = tmsWrapped.getItem();
            for(PropertyAccessor.Localized<?> tmsProperty : tms.getProperties()) {
                propertyValues.put(tmsProperty.key, tmsProperty.delegate.get());
            }
            raw.setItem(tmsWrapped.getParentCategory().getName(), tmsWrapped.getId(), propertyValues);
        }

        File file = new File(ConfigLoaders.MOD_CONFIG_DIRECTORY, FILE_NAME);
        BTETerraRendererConstants.JSON_MAPPER.writeValue(file, raw);
    }
}
