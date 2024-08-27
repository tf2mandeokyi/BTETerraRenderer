package com.mndk.bteterrarenderer.core.loader.yml;

import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;

import java.io.IOException;
import java.util.Map;

public class FlatTileProjectionYamlLoader extends YamlLoader<FlatTileProjectionYamlFile, Map<String, FlatTileProjectionImpl>> {

    public FlatTileProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath, FlatTileProjectionYamlFile.class);
    }

    @Override
    protected Map<String, FlatTileProjectionImpl> load(String fileName, FlatTileProjectionYamlFile content) throws IOException {
        return content.tileProjections;
    }

    @Override
    protected void addToResult(Map<String, FlatTileProjectionImpl> originalT, Map<String, FlatTileProjectionImpl> newT) {
        originalT.putAll(newT);
    }
}
