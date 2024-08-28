package com.mndk.bteterrarenderer.core;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.projection.Proj4jProjection;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManager;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BTETerraRenderer {

    public final String MODID = "bteterrarenderer";
    public final String NAME = "BTETerraRenderer";

    public final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();
    public final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .build();

    public void initialize(ClientMinecraftManager clientManager) {
        McConnector.initialize(clientManager);

        Proj4jProjection.registerProjection();
        ConfigLoaders.setConfigDirectory(McConnector.common().getConfigDirectory(MODID));
        TileMapService.refreshSelectionFromConfig();
    }
}
