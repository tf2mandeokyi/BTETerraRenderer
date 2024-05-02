package com.mndk.bteterrarenderer.core.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtil {

    public static <T> List<T> readJsonList(JsonParser p, JsonParserReadFunction<T> f)
            throws IOException {
        if(p.currentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
        }
        if (p.currentToken() == JsonToken.START_ARRAY) {
            p.nextToken();
        }

        List<T> list = new ArrayList<>();
        do {
            T value = f.read(p);
            list.add(value);
        } while(p.nextToken() != JsonToken.END_ARRAY);
        return list;
    }

    public static double[] readDoubleArray(JsonParser p) throws IOException {
        return readJsonList(p, JsonParser::getDoubleValue)
                .stream().mapToDouble(Double::doubleValue).toArray();
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

    public static JsonNode toBiggerPrimitiveNode(JsonNode node) {
        if((node instanceof ShortNode) || (node instanceof IntNode) || (node instanceof LongNode)) {
            return new BigIntegerNode(node.bigIntegerValue());
        }
        else if((node instanceof FloatNode) || (node instanceof DoubleNode)) {
            return new DecimalNode(node.decimalValue());
        }
        else {
            return node;
        }
    }

    public static JsonNode primitiveToBigNode(Object object) {
        if     (object instanceof JsonNode  ) return (JsonNode) object;

        else if(object instanceof BigDecimal) return new DecimalNode((BigDecimal) object);
        else if(object instanceof Double    ) return new DecimalNode(BigDecimal.valueOf((Double) object));
        else if(object instanceof Float     ) return new DecimalNode(BigDecimal.valueOf((Float) object));

        else if(object instanceof BigInteger) return new BigIntegerNode((BigInteger) object);
        else if(object instanceof Long      ) return new BigIntegerNode(BigInteger.valueOf((Long) object));
        else if(object instanceof Integer   ) return new BigIntegerNode(BigInteger.valueOf((Integer) object));
        else if(object instanceof Short     ) return new BigIntegerNode(BigInteger.valueOf((Short) object));

        else if(object instanceof Boolean   ) return BooleanNode.valueOf((Boolean) object);
        throw new RuntimeException("expected either BigDecimal, BigInteger, Boolean, or JsonNode, " +
                "instead found " + object.getClass());
    }

    public interface JsonParserReadFunction<T> {
        T read(JsonParser p) throws IOException;
    }

}
