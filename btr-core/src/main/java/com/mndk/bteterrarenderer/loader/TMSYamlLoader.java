package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.TileMapService;

import java.io.IOException;
import java.io.Reader;

public class TMSYamlLoader extends YamlLoader<CategoryMap<TileMapService>> {

	public static final TMSYamlLoader INSTANCE = new TMSYamlLoader(
			"maps", "assets/" + BTETerraRendererConstants.MODID + "/default_maps.yml"
	);

	public TMSYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	protected CategoryMap<TileMapService> load(String fileName, Reader fileReader) throws IOException {
		CategoryMap<TileMapService> result =
				BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, new TypeReference<CategoryMap<TileMapService>>() {});
		result.setSource(fileName);
		return result;
	}

	@Override
	protected void addToResult(CategoryMap<TileMapService> originalT, CategoryMap<TileMapService> newT) {
		originalT.append(newT);
	}
}
