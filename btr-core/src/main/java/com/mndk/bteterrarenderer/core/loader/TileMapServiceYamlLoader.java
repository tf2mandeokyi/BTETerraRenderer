package com.mndk.bteterrarenderer.core.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.tile.TileMapService;

import java.io.IOException;
import java.io.Reader;

public class TileMapServiceYamlLoader extends YamlLoader<CategoryMap<TileMapService<?>>> {

	public static final TileMapServiceYamlLoader INSTANCE = new TileMapServiceYamlLoader(
			"maps", "assets/" + BTETerraRendererConstants.MODID + "/default_maps.yml"
	);

	public TileMapServiceYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	protected CategoryMap<TileMapService<?>> load(String fileName, Reader fileReader) throws IOException {
		CategoryMap<TileMapService<?>> result =
				BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, new TypeReference<CategoryMap<TileMapService<?>>>() {});
		result.setSource(fileName);
		return result;
	}

	@Override
	protected void addToResult(CategoryMap<TileMapService<?>> originalT, CategoryMap<TileMapService<?>> newT) {
		originalT.append(newT);
	}
}
