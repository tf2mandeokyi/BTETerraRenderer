package com.mndk.bteterrarenderer.core.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import lombok.val;

import java.io.IOException;
import java.io.Reader;

public class TileMapServiceYamlLoader extends YamlLoader<CategoryMap<TileMapService<?>>> {

	public static final TileMapServiceYamlLoader INSTANCE = new TileMapServiceYamlLoader(
			"maps", "assets/" + BTETerraRendererConstants.MODID + "/default_maps.yml"
	);

	public TileMapServiceYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath);
	}

	@Override
	public void refresh() {
		if(result != null) {
			for (val category : result.getCategories()) {
				for (val entry : category.getValue().entrySet()) {
					entry.getValue().getItem().close();
				}
			}
		}
		super.refresh();
	}

	protected CategoryMap<TileMapService<?>> load(String fileName, Reader fileReader) throws IOException {
		TypeReference<CategoryMap<TileMapService<?>>> typeRef = new TypeReference<CategoryMap<TileMapService<?>>>() {};
		CategoryMap<TileMapService<?>> result = BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, typeRef);
		result.setSource(fileName);
		return result;
	}

	@Override
	protected void addToResult(CategoryMap<TileMapService<?>> originalT, CategoryMap<TileMapService<?>> newT) {
		originalT.append(newT);
	}
}
