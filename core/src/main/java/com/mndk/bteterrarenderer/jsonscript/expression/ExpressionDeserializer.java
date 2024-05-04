package com.mndk.bteterrarenderer.jsonscript.expression;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.expression.control.ClosureExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.define.FunctionCallExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.LiteralExpression;
import lombok.Data;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionDeserializer extends JsonDeserializer<JsonExpression> {

    public static final JavaType EXPRESSION_LIST_JAVATYPE = JsonScript.jsonMapper()
            .constructType(new TypeReference<List<JsonExpression>>() {});

    private static final Map<Class<? extends JsonExpression>, Constructor<? extends JsonExpression>> CONSTRUCTORS =
            new HashMap<>();

    private static final Map<Class<? extends JsonExpression>, ExpressionCreatorParseResult<?>> PARSED_MAP =
            new HashMap<>();

    @Override
    public JsonExpression deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = ctxt.readTree(p);
        switch(node.getNodeType()) {
            case NULL:
            case BOOLEAN:
            case STRING:
            case NUMBER:
                return new LiteralExpression(node);
            case ARRAY:
                List<JsonExpression> expressions = ctxt.readTreeAsValue(node, EXPRESSION_LIST_JAVATYPE);
                return new ClosureExpression(expressions);
            case OBJECT:
                if(node.isEmpty()) {
                    return new LiteralExpression(NullNode.getInstance());
                }
                else if(node.size() != 1) {
                    throw JsonMappingException.from(p, "expected property size of 1, instead found " + node.size());
                }

                Map.Entry<String, JsonNode> entry = node.fields().next();
                String functionName = entry.getKey();
                JsonNode argument = entry.getValue();

                if(Expressions.RESERVED.containsKey(functionName)) {
                    Class<? extends JsonExpression> clazz = Expressions.RESERVED.get(functionName);
                    ExpressionCreatorParseResult<?> parsed = getMemoizedExpressionCreator(clazz);
                    if(parsed != null) {
                        argument = parsed.objectifyArgument(argument);
                    }

                    return ctxt.readTreeAsValue(argument, clazz);
                }
                else {
                    return new FunctionCallExpression(functionName, entry.getValue());
                }

            default:
                throw JsonMappingException.from(p, "unexpected token: " + p.currentToken());
        }
    }

    @Nullable
    private static <T extends JsonExpression>
    ExpressionCreatorParseResult<T> getMemoizedExpressionCreator(Class<T> expressionClass)
    {
        if(PARSED_MAP.containsKey(expressionClass)) {
            return BTRUtil.uncheckedCast(PARSED_MAP.get(expressionClass));
        }

        Constructor<T> constructor = searchExpressionCreator(expressionClass);
        if(constructor == null) {
            PARSED_MAP.put(expressionClass, null);
            return null;
        }

        ExpressionCreatorParseResult<T> result = new ExpressionCreatorParseResult<>(constructor);
        PARSED_MAP.put(expressionClass, result);
        return result;
    }

    @Nullable
    private static <T extends JsonExpression> Constructor<T> searchExpressionCreator(Class<T> expressionClass) {
        if(CONSTRUCTORS.containsKey(expressionClass)) {
            return BTRUtil.uncheckedCast(CONSTRUCTORS.get(expressionClass));
        }

        Constructor<?> result = getExpressionCreator(expressionClass);
        CONSTRUCTORS.put(expressionClass, BTRUtil.uncheckedCast(result));
        return BTRUtil.uncheckedCast(result);
    }

    @Nullable
    private static <T extends JsonExpression> Constructor<?> getExpressionCreator(Class<T> expressionClass) {
        Constructor<?> result = null;
        Constructor<?>[] constructors = expressionClass.getDeclaredConstructors();
        for(Constructor<?> constructor : constructors) {
            if(!constructor.isAnnotationPresent(JsonExpressionCreator.class)) {
                continue;
            }
            if(result != null) {
                throw new RuntimeException("class " + expressionClass + " contains more than two constructors annotated " +
                        "as expression creator");
            }
            result = constructor;
        }
        return result;
    }

    @Data
    private static class ExpressionCreatorParseResult<T extends JsonExpression> {
        private final ParsedParameterTuple[] nameMappings;
        private final int minimumLength;
        @Nullable
        private final String sizeVariableParameterName;

        private ExpressionCreatorParseResult(Constructor<T> constructor) {
            JsonExpressionCreator annotation = constructor.getAnnotation(JsonExpressionCreator.class);
            Parameter[] parameters = constructor.getParameters();

            int minimumLength = 0;
            List<ParsedParameterTuple> nameMappings = new ArrayList<>();
            String sizeVariableParameter = null;

            for(int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];

                JsonProperty paramAnnotation = parameter.getAnnotation(JsonProperty.class);
                if(paramAnnotation == null || JsonProperty.USE_DEFAULT_NAME.equals(paramAnnotation.value())) {
                    throw new IllegalArgumentException("a parameter name must be provided by @JsonProperty annotation");
                }

                String name = paramAnnotation.value();
                if(annotation.variableSize() && i == parameters.length - 1) {
                    sizeVariableParameter = name;
                    break;
                }

                boolean required = paramAnnotation.required();
                if(required) minimumLength++;

                nameMappings.add(new ParsedParameterTuple(name, required));
            }

            this.nameMappings = nameMappings.toArray(new ParsedParameterTuple[0]);
            this.minimumLength = minimumLength;
            this.sizeVariableParameterName = sizeVariableParameter;
        }

        private JsonNode objectifyArgument(JsonNode argument) {
            if(argument.isObject()) {
                return argument;
            }
            else if(!argument.isArray()) {
                throw new IllegalArgumentException("expected array type argument, instead found " + argument.getNodeType());
            }
            return this.objectifyArgument((ArrayNode) argument);
        }

        private JsonNode objectifyArgument(ArrayNode arrayArgument) {
            if(arrayArgument.size() < this.minimumLength) {
                throw new IllegalArgumentException("expected minimum array size of " + this.minimumLength + " but " +
                        arrayArgument.size() + " were given");
            }

            ObjectNode result = JsonScript.jsonMapper().createObjectNode();
            int optionalArgumentLeft = arrayArgument.size() - this.minimumLength;
            int index = 0;

            for(ParsedParameterTuple tuple : this.nameMappings) {
                if(!tuple.isRequired()) {
                    if(optionalArgumentLeft == 0) continue;
                    optionalArgumentLeft--;
                }

                String name = tuple.getName();
                result.set(name, arrayArgument.get(index));
                index++;
            }

            if(optionalArgumentLeft != 0 && this.sizeVariableParameterName == null) {
                throw new IllegalArgumentException("expected maximum array size of " + index + " but " +
                        arrayArgument.size() + " were given");
            }
            else if(this.sizeVariableParameterName != null) {
                ArrayNode variableSizeParamArgument = JsonScript.jsonMapper().createArrayNode();
                for (int i = index; i < arrayArgument.size(); i++) {
                    variableSizeParamArgument.add(arrayArgument.get(i));
                }
                result.set(this.sizeVariableParameterName, variableSizeParamArgument);
            }

            return result;
        }
    }

    @Data
    private static class ParsedParameterTuple {
        private final String name;
        private final boolean required;
    }
}
