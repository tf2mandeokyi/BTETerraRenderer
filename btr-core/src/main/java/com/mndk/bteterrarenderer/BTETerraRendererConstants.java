package com.mndk.bteterrarenderer;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.apache.logging.log4j.Logger;

public class BTETerraRendererConstants {

    public static final String MODID = "bteterrarenderer";
    public static final String NAME = "BTETerraRenderer";

    public static Logger LOGGER;

    public static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();
    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .build();

    public static final GltfModelReader GLTF_MODEL_READER = new GltfModelReader();

    static {
        Proj4jProjection.registerProjection();
    }
}
