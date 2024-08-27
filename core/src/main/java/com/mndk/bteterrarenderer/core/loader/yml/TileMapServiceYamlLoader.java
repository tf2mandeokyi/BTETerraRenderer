package com.mndk.bteterrarenderer.core.loader.yml;

import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.util.Loggers;
import lombok.val;

import java.io.IOException;

public class TileMapServiceYamlLoader extends YamlLoader<TileMapServiceYamlFile, CategoryMap<TileMapService>> {

	public TileMapServiceYamlLoader(String folderName, String defaultYamlPath) {
		super(folderName, defaultYamlPath, TileMapServiceYamlFile.class);
	}

	@Override
	public void refresh() {
		if(result != null) {
			for (val category : result.getCategories()) {
				for (val entry : category.getValue().entrySet()) {
                    try {
                        entry.getValue().getItem().close();
                    } catch (Exception e) {
						Loggers.get(this).error("Couldn't close TMS", e);
                    }
                }
			}
		}
		super.refresh();
	}

	protected CategoryMap<TileMapService> load(String fileName, TileMapServiceYamlFile content) throws IOException {
		CategoryMap<TileMapService> categoryMap = content.getCategories();
		categoryMap.setSource(fileName);
		return categoryMap;
	}

	@Override
	protected void addToResult(CategoryMap<TileMapService> originalT, CategoryMap<TileMapService> newT) {
		originalT.append(newT);
	}
}
