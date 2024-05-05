package com.mndk.bteterrarenderer.jsonscript.parameter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = JsonParameter.Deserializer.class)
public class JsonParameter {
    public static final JavaType LIST_JAVATYPE = JsonScript.jsonMapper()
            .constructType(new TypeReference<List<JsonParameter>>() {});

    private final String name;
    private final ArgumentType argumentType;
    private final ParameterType type;

    static class Deserializer extends JsonDeserializer<JsonParameter> {
        public JsonParameter deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            String name, argumentTypeName;
            if(node.isTextual()) {
                name = node.asText();
                argumentTypeName = "exp";
            }
            else if(node.isObject()) {
                if(node.size() != 1) {
                    throw JsonMappingException.from(p, "expected property size of 1, instead found " + node.size());
                }

                Map.Entry<String, JsonNode> entry = node.fields().next();
                JsonNode value = entry.getValue();
                if(!value.isTextual()) {
                    throw JsonMappingException.from(p, "expected string type, found " + value.getNodeType());
                }

                name = entry.getKey();
                argumentTypeName = value.asText();
            }
            else {
                throw JsonMappingException.from(p, "expected object type, found " + node.getNodeType());
            }

            ParameterType parameterType = ParameterType.NORMAL;
            if(name.endsWith("?...") || name.endsWith("...?")) {
                throw JsonMappingException.from(p, "parameter can't be both optional and size-variable");
            }
            if(name.endsWith("...")) {
                parameterType = ParameterType.VARIABLE;
                name = name.substring(0, name.length() - 3);
            }
            if(name.endsWith("?")) {
                parameterType = ParameterType.OPTIONAL;
                name = name.substring(0, name.length() - 1);
            }

            ArgumentType argumentType;
            try {
                argumentType = ArgumentType.from(argumentTypeName);
            } catch (ArgumentType.ParseException e) {
                throw JsonMappingException.from(p, e.getMessage());
            }
            return new JsonParameter(name, argumentType, parameterType);
        }
    }

}
