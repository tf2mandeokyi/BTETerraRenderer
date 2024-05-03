package com.mndk.bteterrarenderer.jsonscript.exp.func;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ArgumentType {
    Pattern GENERIC_PATTERN = Pattern.compile("^(\\S+)\\s*<(.+)>$");
    StrType STR = new StrType();
    JsonType JSON = new JsonType();
    ExpType EXP = new ExpType();

    ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) throws ParameterParseException;

    static ArgumentType from(String typeName) throws IllegalArgumentException {
        typeName = typeName.trim();
        Matcher genericMatcher = GENERIC_PATTERN.matcher(typeName);
        if(genericMatcher.matches()) {
            String type = genericMatcher.group(1);
            String generic = genericMatcher.group(2);
            switch(type) {
                case "list": return new ListType(from(generic));
                case "obj" : return new ObjType(from(generic));
                default: throw new IllegalArgumentException("unknown type " + type);
            }
        }
        else {
            switch(typeName) {
                case "str" : return STR;
                case "json": return JSON;
                case "exp" : return EXP;
                default: throw new IllegalArgumentException("unknown type " + typeName);
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class StrType implements ArgumentType {
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            if(!argument.isTextual()) {
                throw new IllegalArgumentException("expected string, found: " + argument.getNodeType());
            }
            return ExpressionResult.ok(argument);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class JsonType implements ArgumentType {
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            return ExpressionResult.ok(argument);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class ExpType implements ArgumentType {
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            try {
                JsonExpression expression = JsonScript.jsonMapper().treeToValue(argument, JsonExpression.class);
                return expression.run(runtime, null);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ListType implements ArgumentType {
        private final ArgumentType innerType;
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument)
                throws ParameterParseException
        {
            if(!argument.isArray()) {
                throw new IllegalArgumentException("expected array, found: " + argument.getNodeType());
            }

            ArrayNode arrayNodeResult = JsonScript.jsonMapper().createArrayNode();
            for(JsonNode element : argument) {
                ExpressionResult result = this.innerType.formatArgument(runtime, element);
                if(result.isBreakType()) return result;

                JsonScriptValue value = result.getValue();
                if(!(value instanceof JsonScriptJsonValue)) {
                    throw new ParameterParseException("value must be a json type");
                }
                arrayNodeResult.add(((JsonScriptJsonValue) value).getNode());
            }
            return ExpressionResult.ok(arrayNodeResult);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ObjType implements ArgumentType {
        private final ArgumentType innerType;
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument)
                throws ParameterParseException
        {
            if(!argument.isObject()) {
                throw new IllegalArgumentException("expected object, found: " + argument.getNodeType());
            }

            ObjectNode argumentObject = (ObjectNode) argument;
            ObjectNode objectNodeResult = JsonScript.jsonMapper().createObjectNode();
            for (Iterator<Map.Entry<String, JsonNode>> it = argumentObject.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();

                JsonNode element = entry.getValue();
                ExpressionResult result = this.innerType.formatArgument(runtime, element);
                if(result.isBreakType()) return result;

                String key = entry.getKey();
                JsonScriptValue value = result.getValue();
                if(!(value instanceof JsonScriptJsonValue)) {
                    throw new ParameterParseException("value must be a json type");
                }
                objectNodeResult.set(key, ((JsonScriptJsonValue) value).getNode());
            }
            return ExpressionResult.ok(objectNodeResult);
        }
    }
}
