package com.mndk.bteterrarenderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

public class BTETerraRendererConstants {

    public static final String MODID = "bteterrarenderer";
    public static final String NAME = "BTETerraRenderer";

    public static Logger LOGGER;

    public static final Reflections REFLECTIONS = new Reflections("com.mndk." + MODID);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    static {
        Proj4jProjection.registerProjection();
    }
}
