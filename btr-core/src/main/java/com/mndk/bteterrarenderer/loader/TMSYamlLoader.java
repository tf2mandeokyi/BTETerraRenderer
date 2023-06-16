package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.FlatTileMapService;

import java.io.IOException;
import java.io.Reader;

public class TMSYamlLoader extends YamlLoader<CategoryMapData<FlatTileMapService>> {

	public static final TMSYamlLoader INSTANCE = new TMSYamlLoader(
			"maps", "assets/" + BTETerraRendererConstants.MODID + "/default_maps.yml"
	);

	public TMSYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	protected CategoryMapData<FlatTileMapService> load(String fileName, Reader fileReader) throws IOException {
		CategoryMapData<FlatTileMapService> result =
				BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, new TypeReference<CategoryMapData<FlatTileMapService>>() {});
		result.setSource(fileName);
		return result;
	}

	@Override
	protected void addToResult(CategoryMapData<FlatTileMapService> originalT, CategoryMapData<FlatTileMapService> newT) {
		originalT.append(newT);
	}
}
