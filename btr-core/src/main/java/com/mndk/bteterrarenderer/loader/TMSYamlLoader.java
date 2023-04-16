package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.TileMapService;

import java.io.IOException;
import java.io.Reader;

public class TMSYamlLoader extends YamlLoader<CategoryMapData<TileMapService>> {

	public static final TMSYamlLoader INSTANCE = new TMSYamlLoader(
			"maps", "assets/" + BTETerraRendererConstants.MODID + "/default_maps.yml"
	);

	public TMSYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	protected CategoryMapData<TileMapService> load(String fileName, Reader fileReader) throws IOException {
		CategoryMapData<TileMapService> result =
				BTETerraRendererConstants.OBJECT_MAPPER.readValue(fileReader, new TypeReference<CategoryMapData<TileMapService>>() {});
		result.setSource(fileName);
		return result;
	}

	@Override
	protected void addToResult(CategoryMapData<TileMapService> originalT, CategoryMapData<TileMapService> newT) {
		originalT.append(newT);
	}
}
