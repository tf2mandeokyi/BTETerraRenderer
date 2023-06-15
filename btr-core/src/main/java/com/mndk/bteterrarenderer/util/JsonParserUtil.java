package com.mndk.bteterrarenderer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtil {

    public static <T> List<T> readJsonList(JsonParser p, boolean readFromArrayStart, JsonParserReadFunction<T> f) throws IOException {
        if(readFromArrayStart) {
            if (p.nextToken() != JsonToken.START_ARRAY) {
                throw JsonMappingException.from(p, "expected array start, found: " + p.currentToken());
            }
        }

        JsonToken token;
        List<T> list = new ArrayList<>();
        while((token = p.nextToken()) != JsonToken.END_ARRAY) {
            if(!token.isNumeric()) {
                throw JsonMappingException.from(p, "expected number element, found: " + token);
            }
            T value = f.read(p);
            list.add(value);
        }
        return list;
    }

    public static double[] readDoubleArray(JsonParser p, boolean readArrayFromStart) throws IOException {
        return readJsonList(p, readArrayFromStart, JsonParser::getDoubleValue)
                .stream().mapToDouble(Double::doubleValue).toArray();
    }

    public interface JsonParserReadFunction<T> {
        T read(JsonParser p) throws IOException;
    }

}
