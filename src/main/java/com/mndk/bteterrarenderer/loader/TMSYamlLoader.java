package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.DropdownCategory;
import com.mndk.bteterrarenderer.tile.TileMapService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TMSYamlLoader extends YamlLoader<CategoryLoaderResult<TileMapService>> {

	public static final TMSYamlLoader INSTANCE = new TMSYamlLoader(
			"maps", "assets/" + BTETerraRenderer.MODID + "/default_maps.yml"
	);

	public TMSYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	@SuppressWarnings("unchecked")
	protected CategoryLoaderResult<TileMapService> load(String fileName, Map<String, Object> data) {

		Map<String, Object> categories = (Map<String, Object>) data.get("categories");
		List<DropdownCategory<TileMapService>> result = new ArrayList<>();
		
		for(Map.Entry<String, Object> category : categories.entrySet()) {
			result.add(getCategoryFromObject(
					category.getKey(), (Map<String, Object>) category.getValue(), fileName
			));
		}
		return new CategoryLoaderResult<>(result);
	}

	@SuppressWarnings("unchecked")
	protected DropdownCategory<TileMapService> getCategoryFromObject(
			String categoryName, Map<String, Object> mapsObject, String mapFile
	) {
		List<TileMapService> mapList = new ArrayList<>();

		for(Map.Entry<String, Object> map : mapsObject.entrySet()) {
			mapList.add(new TileMapService(mapFile, categoryName, map.getKey(), (Map<String, Object>) map.getValue()));
		}

		return new DropdownCategory<>(categoryName, mapList);
	}

	@Override
	protected void addToResult(CategoryLoaderResult<TileMapService> originalT, CategoryLoaderResult<TileMapService> newT) {
		originalT.append(newT);
	}
}
