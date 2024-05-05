package com.mndk.bteterrarenderer.jsonscript.parameter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ErrorMessages;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonDeserialize(using = JsonParameters.Deserializer.class)
public abstract class JsonParameters {

    public final ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode argument,
                                           Map<String, JsonScriptValue> target, ExpressionCallerInfo callerInfo) {
        return this.evaluate(runtime, argument, target).passedBy(callerInfo);
    }

    protected abstract ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode argument,
                                                 Map<String, JsonScriptValue> target);

    static class Deserializer extends JsonDeserializer<JsonParameters> {
        public JsonParameters deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            if(node.isObject() || node.isTextual()) {
                JsonParameter parameter = ctxt.readTreeAsValue(node, JsonParameter.class);
                return new Single(parameter);
            }
            else if(node.isArray()) {
                List<JsonParameter> parameters = ctxt.readTreeAsValue(node, JsonParameter.LIST_JAVATYPE);
                return new Multiple(parameters);
            }
            throw JsonMappingException.from(p, "expected either object, array, or string, " +
                    "instead found " + node.getNodeType());
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
        protected ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode argument,
                                            Map<String, JsonScriptValue> target)
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
        protected ExpressionResult evaluate(JsonScriptRuntime runtime, JsonNode arguments,
                                            Map<String, JsonScriptValue> target)
        {
            if(arguments.isArray()) {
                return this.evaluate(runtime, (ArrayNode) arguments, target);
            }
            else if(arguments.isObject()) {
                return this.evaluate(runtime, (ObjectNode) arguments, target);
            }
            return ExpressionResult.error("expected array or object argument, " +
                    "instead found " + arguments.getNodeType(), null);
        }

        private ExpressionResult evaluate(JsonScriptRuntime runtime, ArrayNode arguments,
                                          Map<String, JsonScriptValue> target)
        {
            for(int argumentIndex = 0; argumentIndex < arguments.size(); argumentIndex++) {
                JsonParameter parameter = this.getParameterByIndex(argumentIndex);
                if(parameter == null) {
                    return ExpressionResult.error("argument index overflow", null);
                }

                JsonNode argument = arguments.get(argumentIndex);
                ExpressionResult result = this.insertArgument(runtime, parameter, argument, target);
                if(result.isBreakType()) return result;
            }
            return this.fillEmptyArguments(target);
        }

        @Nullable
        private JsonParameter getParameterByIndex(int argumentIndex) {
            if(argumentIndex < this.positionMapping.length) {
                return this.positionMapping[argumentIndex];
            }
            return this.variableParameter;
        }

        private ExpressionResult evaluate(JsonScriptRuntime runtime, ObjectNode arguments,
                                          Map<String, JsonScriptValue> target)
        {
            for(Iterator<String> it = arguments.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                JsonNode argument = arguments.get(fieldName);

                JsonParameter parameter = this.nameArgTypeMap.get(fieldName);
                if(parameter == null) {
                    // TODO: support python-kwargs-style arguments???
                    return ExpressionResult.error("function does not have a parameter named \"" + fieldName + "\"", null);
                }

                if(parameter.getType() != ParameterType.VARIABLE) {
                    ExpressionResult result = this.insertArgument(runtime, parameter, argument, target);
                    if(result.isBreakType()) return result;
                    continue;
                }

                ArgumentType argType = parameter.getArgumentType();
                ResultTransformer.JVal transformer = argType.formatArgument(runtime, argument)
                        .transformer()
                        .asJsonValue(ErrorMessages.valueMustBeJson("value"), null)
                        .asNode()
                        .checkIfArray(type -> "encountered non-array for variable argument \"" + fieldName + "\" (" + type + ")", null)
                        .asValue();
                if(transformer.isBreakType()) return transformer.getResult();

                target.put(fieldName, transformer.getWrapped());
            }
            return this.fillEmptyArguments(target);
        }

        private ExpressionResult insertArgument(JsonScriptRuntime runtime, JsonParameter parameter,
                                                JsonNode argument, Map<String, JsonScriptValue> target)
        {
            String name = parameter.getName();
            if(target.containsKey(name)) {
                return ExpressionResult.error("parameter name \"" + name + "\" collides", null);
            }

            ArgumentType argType = parameter.getArgumentType();
            ExpressionResult result = argType.formatArgument(runtime, argument);
            if(result.isBreakType()) return result;

            JsonScriptValue value = result.getValue();
            ParameterType paramType = parameter.getType();

            switch(paramType) {
                case NORMAL:
                case OPTIONAL:
                    target.put(name, value);
                    break;
                case VARIABLE:
                    if(!(value instanceof JsonScriptJsonValue)) {
                        return ExpressionResult.error("value must be a json type", null);
                    }

                    target.computeIfAbsent(name, key -> JsonScriptValue.emptyArray())
                            .transformer()
                            .asJsonValue(type -> "this error will never be thrown", null)
                            .asArrayNode(type -> "this error will never be thrown", null)
                            .getWrapped()
                            .add(((JsonScriptJsonValue) value).getNode());
                    break;
            }
            return ExpressionResult.ok();
        }

        private ExpressionResult fillEmptyArguments(Map<String, JsonScriptValue> target) {
            for(JsonParameter parameter : this.positionMapping) {
                String name = parameter.getName();
                if(parameter.getType() == ParameterType.NORMAL) {
                    return ExpressionResult.error("argument missing for parameter \"" + name + "\"", null);
                }
                if(target.containsKey(name)) continue;
                target.put(name, JsonScriptValue.jsonNull());
            }

            if(this.variableParameter != null) {
                String variableName = this.variableParameter.getName();
                if(!target.containsKey(variableName)) {
                    target.put(variableName, JsonScriptValue.emptyArray());
                }
            }
            return ExpressionResult.ok();
        }
    }
}
