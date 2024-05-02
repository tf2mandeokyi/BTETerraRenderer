package com.mndk.bteterrarenderer.jsonscript.exp.func;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.ParameterType;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class JsonParameters {

    public abstract ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode argument,
                                              Map<String, JsonScriptValue> target) throws ExpressionRunException;

    public static JsonParameters single(JsonParameter parameter) {
        return new Single(parameter);
    }

    public static JsonParameters multiple(List<JsonParameter> parameters) {
        return new Multiple(parameters);
    }

    public static class Deserializer extends JsonDeserializer<JsonParameters> {
        public JsonParameters deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            if(node.isObject() || node.isTextual()) {
                JsonParameter parameter = ctxt.readTreeAsValue(node, JsonParameter.class);
                return single(parameter);
            }
            else if(node.isArray()) {
                List<JsonParameter> parameters = ctxt.readTreeAsValue(node, JsonParameter.LIST_JAVATYPE);
                return multiple(parameters);
            }
            throw JsonMappingException.from(p, "expected either object, array, or string, " +
                    "but instead found " + node.getNodeType());
        }
    }

    private static class Single extends JsonParameters {
        private final JsonParameter parameter;

        private Single(JsonParameter parameter) {
            switch(parameter.getType()) {
                case OPTIONAL:
                    throw new IllegalArgumentException("single-parameter functions cannot have optional arguments");
                case VARIABLE:
                    throw new IllegalArgumentException("single-parameter functions cannot have variable-sized arguments");
            }
            this.parameter = parameter;
        }

        @Override
        public ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode argument,
                                         Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            String parameterName = this.parameter.getName();
            ArgumentType parameterType = this.parameter.getArgumentType();

            ExpressionResult result = parameterType.formatArgument(runtime, argument);
            if(result.isBreakType()) return result;

            target.put(parameterName, result.getValue());
            return ExpressionResult.ok();
        }
    }

    private static class Multiple extends JsonParameters {
        private final Map<String, JsonParameter> nameArgTypeMap = new HashMap<>();
        /**
         * Contains normal and optional parameters. Does not contain variable parameter
         */
        private final JsonParameter[] positionMapping;
        @Nullable
        private final JsonParameter variableParameter;

        private Multiple(List<JsonParameter> parameters) {
            this.positionMapping = new JsonParameter[parameters.size()];

            ParameterType parsingMode = ParameterType.NORMAL;
            JsonParameter variableParameter = null;

            for(int i = 0; i < parameters.size(); i++) {
                JsonParameter parameter = parameters.get(i);
                String parameterName = parameter.getName();
                if (nameArgTypeMap.containsKey(parameterName)) {
                    throw new IllegalArgumentException("duplicate parameter name: \"" + parameterName + "\"");
                }

                ParameterType type = parameter.getType();
                if(type.getPositionOrder() < parsingMode.getPositionOrder()) {
                    throw new IllegalArgumentException("function parameters should be ordered like " +
                            "[ normal, optional?, variable... ]");
                }

                if(type == ParameterType.VARIABLE) {
                    variableParameter = parameter;
                    if (i != parameters.size() - 1) {
                        throw new IllegalArgumentException("no parameters should come after a variable-size parameter");
                    }
                    break;
                }

                parsingMode = type;
                positionMapping[i] = parameter;
                nameArgTypeMap.put(parameterName, parameter);
            }
            this.variableParameter = variableParameter;
        }

        @Override
        public ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode arguments,
                                         Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            if(arguments.isArray()) {
                return this.evaluate(runtime, (ArrayNode) arguments, target);
            }
            else if(arguments.isObject()) {
                return this.evaluate(runtime, (ObjectNode) arguments, target);
            }
            throw runtime.exception("expected array or object argument, found " + arguments.getNodeType());
        }

        private ExpressionResult evaluate(JsonScriptRuntime runtime, ArrayNode arguments,
                                          Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            for(int argumentIndex = 0; argumentIndex < arguments.size(); argumentIndex++) {
                JsonParameter parameter = this.getParameterByIndex(runtime, argumentIndex);
                JsonNode argument = arguments.get(argumentIndex);

                ExpressionResult result = this.insertArgument(runtime, parameter, argument, target);
                if(result.isBreakType()) return result;
            }
            this.fillEmptyArguments(runtime, target);
            return ExpressionResult.ok();
        }

        private ExpressionResult evaluate(JsonScriptRuntime runtime, ObjectNode arguments,
                                          Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            for(Iterator<String> it = arguments.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                JsonNode argument = arguments.get(fieldName);

                JsonParameter parameter = this.nameArgTypeMap.get(fieldName);
                if(parameter == null) {
                    // TODO: support python-kwargs-style arguments???
                    throw new ExpressionRunException("function does not have a parameter named \"" + fieldName + "\"");
                }

                if(parameter.getType() != ParameterType.VARIABLE) {
                    ExpressionResult result = this.insertArgument(runtime, parameter, argument, target);
                    if(result.isBreakType()) return result;
                    continue;
                }

                ArgumentType argType = parameter.getArgumentType();
                ExpressionResult result = argType.formatArgument(runtime, argument);
                if(result.isBreakType()) return result;

                JsonScriptValue value = result.getValue();
                JsonNode nodeValue = value.getAsJsonValue();
                if(!nodeValue.isArray()) {
                    throw runtime.exception("expected array for variable argument \"" + fieldName + "\", " +
                            "instead found " + nodeValue.getNodeType());
                }
                target.put(fieldName, value);
            }

            this.fillEmptyArguments(runtime, target);
            return ExpressionResult.ok();
        }

        private JsonParameter getParameterByIndex(JsonScriptRuntime runtime,
                                                  int argumentIndex) throws ExpressionRunException
        {
            if(argumentIndex < this.positionMapping.length) {
                return this.positionMapping[argumentIndex];
            }

            JsonParameter parameter = this.variableParameter;
            if(parameter == null) {
                throw runtime.exception("argument index overflow");
            }
            return parameter;
        }

        private ExpressionResult insertArgument(JsonScriptRuntime runtime, JsonParameter parameter,
                                                JsonNode argument,
                                                Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            ArgumentType argType = parameter.getArgumentType();
            ExpressionResult result = argType.formatArgument(runtime, argument);
            if(result.isBreakType()) return result;

            JsonScriptValue value = result.getValue();
            return this.insertArgument(parameter, value, target);
        }

        private ExpressionResult insertArgument(JsonParameter parameter, JsonScriptValue value,
                                                Map<String, JsonScriptValue> target) throws ExpressionRunException
        {
            String name = parameter.getName();
            ParameterType paramType = parameter.getType();

            switch(paramType) {
                case NORMAL:
                case OPTIONAL:
                    target.put(name, value);
                    break;
                case VARIABLE:
                    JsonScriptValue variableValue = target.computeIfAbsent(name, key -> JsonScriptValue.emptyArray());
                    ArrayNode arrayNode = (ArrayNode) variableValue.getAsJsonValue();
                    arrayNode.add(value.getAsJsonValue());
                    break;
            }
            return ExpressionResult.ok();
        }

        private void fillEmptyArguments(JsonScriptRuntime runtime, Map<String, JsonScriptValue> target)
                throws ExpressionRunException
        {
            for(JsonParameter parameter : this.positionMapping) {
                String name = parameter.getName();
                if(parameter.getType() == ParameterType.NORMAL) {
                    throw runtime.exception("argument missing for parameter \"" + name + "\"");
                }
                if(target.containsKey(name)) continue;
                target.put(name, JsonScriptValue.jsonNull());
            }

            if(this.variableParameter != null) {
                String variableName = this.variableParameter.getName();
                if(target.containsKey(variableName)) return;
                target.put(variableName, JsonScriptValue.emptyArray());
            }
        }
    }
}
