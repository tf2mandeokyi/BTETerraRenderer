package com.mndk.bteterrarenderer.connector.terraplusplus;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;

import java.io.IOException;
import java.io.Reader;

public interface JacksonConnector {
    JacksonConnector INSTANCE = ImplFinder.search(JacksonConnector.class);

    ProjectionYamlLoader.ProjectionYamlFile readYamlProjectionFile(Reader reader) throws IOException;
    CategoryMapData<TileMapService> readYamlTileMapServices(Reader reader) throws IOException;
}
