package com.mndk.bteterrarenderer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.Reader;

/**
 * Should've used JSON as a config format instead of YAML in the first place... <br>
 * Hate writing this utility class :/
 */
public class TppDepJacksonYAMLReader {


    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.ObjectMapper TPP_DEP_JACKSON_MAPPER
            = new net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Converts YAML to {@link Object} using btr-dependency jackson,
     * and then converts that back to JSON to {@link T} using tpp-dependency jackson.
     */
    public static <T> T read(Reader fileReader, TypeReference<T> typeReference) throws IOException {
        String json = yamlToJson(fileReader);
        return TPP_DEP_JACKSON_MAPPER.readValue(json, typeReference);
    }

    private static String yamlToJson(Reader fileReader) throws IOException {
        Object obj = JACKSON_MAPPER.readValue(fileReader, Object.class);
        return JACKSON_MAPPER.writeValueAsString(obj);
    }

    static {
        JACKSON_MAPPER.findAndRegisterModules();
        TPP_DEP_JACKSON_MAPPER.findAndRegisterModules();
    }

}
