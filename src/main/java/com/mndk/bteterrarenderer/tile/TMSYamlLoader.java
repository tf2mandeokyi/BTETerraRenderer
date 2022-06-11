package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TMSYamlLoader {
	
	private static final String DEFAULT_MAP_YAML_PATH = "assets/" + BTETerraRenderer.MODID + "/default_maps.yml";
	public static final Yaml YAML = new Yaml();

	private static File mapFilesDirectory;
	public static TMSLoaderResult result;

	public static File getMapFilesDirectory() {
		return mapFilesDirectory;
	}

	public static void refresh() throws Exception {

		result = new TMSLoaderResult();
		
		result.append(loadDefaultMap());
		
		if(!mapFilesDirectory.exists() && !mapFilesDirectory.mkdirs()) {
			throw new Exception("Map folder creation failed.");
		}
		File[] mapFiles = mapFilesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));

		if (mapFiles != null) {
			for (File mapFile : mapFiles) {
				try {
					String name = mapFile.getName();
					FileReader fileReader = new FileReader((mapFile));
					result.append(load(name, fileReader));
					fileReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void refresh(String modConfigDirectory) throws Exception {
		mapFilesDirectory = new File(modConfigDirectory + "/" + BTETerraRenderer.MODID + "/maps");
		refresh();
	}
	

	@SuppressWarnings("unchecked")
	private static TMSLoaderResult load(String fileName, Reader fileReader) {
		
		Map<String, Object> mapData = YAML.load(fileReader);
		Map<String, Object> categories = (Map<String, Object>) mapData.get("categories");
		
		List<TMSLoaderResult.Category> result = new ArrayList<>();
		
		for(Map.Entry<String, Object> category : categories.entrySet()) {
			result.add(getMapCategoryFromMapObject(
					category.getKey(), (Map<String, Object>) category.getValue(), fileName
			));
		}
		return new TMSLoaderResult(result);
	}
	
	
	private static TMSLoaderResult loadDefaultMap() {
		return load("default", new InputStreamReader(
				Objects.requireNonNull(TMSYamlLoader.class.getClassLoader().getResourceAsStream(DEFAULT_MAP_YAML_PATH)),
				StandardCharsets.UTF_8
		));
	}
	
	
	@SuppressWarnings("unchecked")
	private static TMSLoaderResult.Category getMapCategoryFromMapObject(
			String categoryName, Map<String, Object> mapList, String mapFile
	) {
		List<TileMapService> mapSet = new ArrayList<>();

		for(Map.Entry<String, Object> map : mapList.entrySet()) {
			mapSet.add(new TileMapService(mapFile, categoryName, map.getKey(), (Map<String, Object>) map.getValue()));
		}
		
		return new TMSLoaderResult.Category(categoryName, mapSet);
	}
}
