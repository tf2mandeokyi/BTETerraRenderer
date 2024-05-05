package com.mndk.bteterrarenderer.jsonscript.expression.function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.*;

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
    @ArrayArgumentAcceptable
    public StringTemplateFunction(@JsonProperty(value = "template", required = true) JsonExpression template,
                                  @JsonProperty(value = "parameters", required = true) Map<String, JsonExpression> parameters) {
        this.template = template;
        this.parameters = parameters;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ResultTransformer.Str transformer = this.template.run(runtime, TEMPLATE_INFO)
                .transformer()
                .asJsonValue(ErrorMessages.valueMustBeJson("value"), TEMPLATE_INFO)
                .asNode()
                .asText(ErrorMessages.nodeMustBeTextual("template"), TEMPLATE_INFO);
        if(transformer.isBreakType()) return transformer.getResult();

        String template = transformer.getWrapped();
        for (Map.Entry<String, JsonExpression> entry : this.parameters.entrySet()) {
            String key = entry.getKey();
            ExpressionCallerInfo paramInfo = PARAM_INFO.add(key);

            ResultTransformer.Str paramTransformer = entry.getValue().run(runtime, paramInfo)
                    .transformer()
                    .asJsonValue(ErrorMessages.valueMustBeJson("value"), paramInfo)
                    .asNode()
                    .asCastedText(type -> "string-template only accept strings, numbers, booleans and " +
                            "null as parameters, instead " + type + " was given", paramInfo);
            if(paramTransformer.isBreakType()) return paramTransformer.getResult();

            String replace = paramTransformer.getWrapped();
            template = template.replaceAll("\\{" + key + "}", replace);
        }

        return ExpressionResult.ok(new TextNode(template));
    }
}
