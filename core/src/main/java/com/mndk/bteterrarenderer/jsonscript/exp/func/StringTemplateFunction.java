package com.mndk.bteterrarenderer.jsonscript.exp.func;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import java.util.Map;

@JsonDeserialize
public class StringTemplateFunction extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(StringTemplateFunction.class);
    private static final ExpressionCallerInfo TEMPLATE_INFO = INFO.add("template");
    private static final ExpressionCallerInfo PARAM_INFO = INFO.add("parameter");

    private final JsonExpression template;
    private final Map<String, JsonExpression> parameters;

    @JsonCreator
    @JsonExpressionCreator
    public StringTemplateFunction(@JsonProperty(value = "template", required = true) JsonExpression template,
                                  @JsonProperty(value = "parameters", required = true) Map<String, JsonExpression> parameters) {
        this.template = template;
        this.parameters = parameters;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = this.template.run(runtime, INFO);
        if(result.isBreakType()) return result;

        JsonScriptValue value = result.getValue();
        if(!(value instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("value must be a json type", TEMPLATE_INFO);
        }

        JsonNode templateNode = ((JsonScriptJsonValue) value).getNode();
        if(!templateNode.isTextual()) {
            return ExpressionResult.error("template should be textual", TEMPLATE_INFO);
        }
        String template = templateNode.asText();

        for (Map.Entry<String, JsonExpression> entry : this.parameters.entrySet()) {
            String key = entry.getKey();

            result = entry.getValue().run(runtime, INFO);
            if(result.isBreakType()) return result;

            value = result.getValue();
            if(!(value instanceof JsonScriptJsonValue)) {
                return ExpressionResult.error("value must be a json type", PARAM_INFO.add(key));
            }

            JsonNode replaceNode = ((JsonScriptJsonValue) value).getNode();
            String replace;
            if(replaceNode.isTextual()) {
                replace = replaceNode.asText();
            }
            else if(replaceNode.isNumber() || replaceNode.isBoolean() || replaceNode.isNull()) {
                replace = replaceNode.toString();
            }
            else {
                return ExpressionResult.error("string-template only accept strings, numbers, booleans and " +
                        "null as parameters", PARAM_INFO.add(key));
            }

            template = template.replaceAll("\\{" + key + "}", replace);
        }

        return ExpressionResult.ok(new TextNode(template));
    }
}
