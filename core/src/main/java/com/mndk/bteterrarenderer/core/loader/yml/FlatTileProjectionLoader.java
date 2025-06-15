package com.mndk.bteterrarenderer.core.loader.yml;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.util.loader.YamlLoader;
import com.mndk.bteterrarenderer.util.merge.MapMergeStrategy;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjection;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;

import java.io.IOException;
import java.util.Map;

public class FlatTileProjectionLoader extends YamlLoader<FlatTileProjectionDTO, Map<String, FlatTileProjectionImpl>> {

    public FlatTileProjectionLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath, FlatTileProjectionDTO.class, new MapMergeStrategy<>());
    }

    public FlatTileProjection get(JsonNode projectionNode) throws IOException {
        if (projectionNode == null) {
            // Throw exception if projection is not defined
            throw new IOException("projection is not defined");
        }
        if (projectionNode.isTextual()) {
            String projectionName = projectionNode.asText();
            FlatTileProjection projection = this.getResult().get(projectionName);
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
    protected Map<String, FlatTileProjectionImpl> load(String fileName, FlatTileProjectionDTO content) {
        return content.getTileProjections();
    }
}
