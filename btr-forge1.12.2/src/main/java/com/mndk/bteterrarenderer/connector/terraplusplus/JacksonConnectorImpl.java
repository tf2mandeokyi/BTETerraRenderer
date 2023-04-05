package com.mndk.bteterrarenderer.connector.terraplusplus;

import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;

import java.io.IOException;
import java.io.Reader;

public class JacksonConnectorImpl implements JacksonConnector {
    public ProjectionYamlLoader.ProjectionYamlFile readYamlProjectionFile(Reader reader) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }

    public CategoryMapData<TileMapService> readYamlTileMapServices(Reader reader) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }
}
