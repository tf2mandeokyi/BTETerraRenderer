package com.mndk.bteterrarenderer.core.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtil {

    public static <T> List<T> readJsonList(JsonParser p, JsonParserReadFunction<T> f)
            throws IOException {
        if (p.currentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
        }
        if (p.currentToken() == JsonToken.START_ARRAY) {
            p.nextToken();
        }

        JsonToken token = p.currentToken();
        List<T> list = new ArrayList<>();
        do {
            if (!token.isNumeric()) {
                throw JsonMappingException.from(p, "expected number element, found: " + token);
            }
            T value = f.read(p);
            list.add(value);
        } while((token = p.nextToken()) != JsonToken.END_ARRAY);
        return list;
    }

    public static double[] readDoubleArray(JsonParser p) throws IOException {
        return readJsonList(p, JsonParser::getDoubleValue)
                .stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static double getOrDefault(JsonNode node, String property, double defaultValue) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null ? defaultValue : propertyNode.asDouble(defaultValue);
    }

    public static int getOrDefault(JsonNode node, String property, int defaultValue) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null ? defaultValue : propertyNode.asInt(defaultValue);
    }

    public static boolean getOrDefault(JsonNode node, String property, boolean defaultValue) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null ? defaultValue : propertyNode.asBoolean(defaultValue);
    }

    public static String getOrDefault(JsonNode node, String property, String defaultValue) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null ? defaultValue : propertyNode.asText(defaultValue);
    }

    public interface JsonParserReadFunction<T> {
        T read(JsonParser p) throws IOException;
    }

}
