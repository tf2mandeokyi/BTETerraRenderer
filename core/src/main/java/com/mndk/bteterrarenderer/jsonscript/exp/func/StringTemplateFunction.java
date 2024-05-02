package com.mndk.bteterrarenderer.jsonscript.exp.func;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;

import javax.annotation.Nonnull;
import java.util.Map;

@JsonDeserialize
public class StringTemplateFunction implements JsonExpression {

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
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        ExpressionResult result = this.template.run(runtime);
        if(result.isBreakType()) return result;

        JsonNode templateNode = result.getValue().getAsJsonValue();
        if(!templateNode.isTextual()) {
            throw runtime.exception("template should be textual");
        }
        String template = templateNode.asText();

        for (Map.Entry<String, JsonExpression> entry : this.parameters.entrySet()) {
            result = entry.getValue().run(runtime);
            if(result.isBreakType()) return result;

            JsonNode replaceNode = result.getValue().getAsJsonValue();
            String replace;
            if(replaceNode.isTextual()) {
                replace = replaceNode.asText();
            }
            else if(replaceNode.isNumber() || replaceNode.isBoolean() || replaceNode.isNull()) {
                replace = replaceNode.toString();
            }
            else {
                throw runtime.exception("string-template only accept strings, numbers, booleans and null as parameters");
            }

            template = template.replaceAll("\\{" + entry.getKey() + "}", replace);
        }

        return ExpressionResult.ok(new TextNode(template));
    }
}
