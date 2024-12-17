package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjection;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;

import java.io.IOException;
import java.util.Map;

public class FlatTileProjectionYamlLoader extends YamlLoader<FlatTileProjectionYamlFile, Map<String, FlatTileProjectionImpl>> {

    public FlatTileProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath, FlatTileProjectionYamlFile.class);
    }

    public FlatTileProjection get(JsonNode projectionNode) throws IOException {
        if (projectionNode == null) {
            // Throw exception if projection is not defined
            throw new IOException("projection is not defined");
        }
        if (projectionNode.isTextual()) {
            String projectionName = projectionNode.asText();
            FlatTileProjection projection = ConfigLoaders.flatProj().getResult().get(projectionName);
            if (projection == null) {
                throw new IOException("unknown projection name: " + projectionName);
            }
            return projection;
        }
        else if (projectionNode.isObject()) {
            // Do not set projection name for this anonymous value.
            return BTETerraRenderer.JSON_MAPPER.treeToValue(projectionNode, FlatTileProjectionImpl.class);
        }
        else throw new IOException("projection should be an object or a name");
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
