package com.mndk.bteterrarenderer.util.reader;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.Reader;

/**
 * Should've used JSON as a config format instead of YAML in the first place... <br>
 * Hate writing this utility class :/
 */
public class TppDepJacksonYAMLReader {


    private static final com.fasterxml.jackson.databind.ObjectMapper BTR_DEP_JACKSON_MAPPER
            = new com.fasterxml.jackson.databind.ObjectMapper(new YAMLFactory());
    private static final net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.ObjectMapper TPP_DEP_JACKSON_MAPPER
            = new net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Converts YAML to {@link Object} using btr-dependency jackson,
     * and then converts that back to JSON to {@link T} using tpp-dependency jackson.
     */
    public static <T> T read(Reader fileReader, TypeReference<T> typeReference) throws IOException {
        Object obj = BTR_DEP_JACKSON_MAPPER.readValue(fileReader, Object.class);
        String json = TPP_DEP_JACKSON_MAPPER.writeValueAsString(obj);
        return TPP_DEP_JACKSON_MAPPER.readValue(json, typeReference);
    }

    static {
        BTR_DEP_JACKSON_MAPPER.findAndRegisterModules();
        TPP_DEP_JACKSON_MAPPER.findAndRegisterModules();
    }

}
