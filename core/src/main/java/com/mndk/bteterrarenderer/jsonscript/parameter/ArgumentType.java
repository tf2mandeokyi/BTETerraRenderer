package com.mndk.bteterrarenderer.jsonscript.parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ErrorMessages;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;
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

    ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument);

    static ArgumentType from(String typeName) throws ParseException {
        typeName = typeName.trim();
        Matcher genericMatcher = GENERIC_PATTERN.matcher(typeName);
        if(genericMatcher.matches()) {
            String type = genericMatcher.group(1);
            String generic = genericMatcher.group(2);
            switch(type) {
                case "list": return new ListType(from(generic));
                case "obj" : return new ObjType(from(generic));
                default: throw new ParseException("unknown type " + type);
            }
        }
        else {
            switch(typeName) {
                case "str" : return STR;
                case "json": return JSON;
                case "exp" : return EXP;
                default: throw new ParseException("unknown type " + typeName);
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class StrType implements ArgumentType {
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            if(!argument.isTextual()) {
                return ExpressionResult.error("expected string, found: " + argument.getNodeType(), null);
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
                return ExpressionResult.error(e.getMessage(), null);
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ListType implements ArgumentType {
        private final ArgumentType innerType;
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            if(!argument.isArray()) {
                return ExpressionResult.error("expected array, found: " + argument.getNodeType(), null);
            }

            ArrayNode arrayNodeResult = JsonScript.jsonMapper().createArrayNode();
            for(JsonNode element : argument) {
                ResultTransformer.JNode transformer = this.innerType.formatArgument(runtime, element)
                        .transformer()
                        .asJsonValue(ErrorMessages.valueMustBeJson("value"), null)
                        .asNode();
                if(transformer.isBreakType()) return transformer.getResult();

                arrayNodeResult.add(transformer.getWrapped());
            }
            return ExpressionResult.ok(arrayNodeResult);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ObjType implements ArgumentType {
        private final ArgumentType innerType;
        public ExpressionResult formatArgument(JsonScriptRuntime runtime, JsonNode argument) {
            if(!argument.isObject()) {
                return ExpressionResult.error("expected object, found: " + argument.getNodeType(), null);
            }

            ObjectNode argumentObject = (ObjectNode) argument;
            ObjectNode objectNodeResult = JsonScript.jsonMapper().createObjectNode();
            for(Iterator<Map.Entry<String, JsonNode>> it = argumentObject.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();

                JsonNode element = entry.getValue();
                ResultTransformer.JNode transformer = this.innerType.formatArgument(runtime, element)
                        .transformer()
                        .asJsonValue(ErrorMessages.valueMustBeJson("value"), null)
                        .asNode();
                if(transformer.isBreakType()) return transformer.getResult();

                String key = entry.getKey();
                objectNodeResult.set(key, transformer.getWrapped());
            }
            return ExpressionResult.ok(objectNodeResult);
        }
    }

    class ParseException extends Exception {
        private ParseException(String message) {
            super(message);
        }
    }
}
