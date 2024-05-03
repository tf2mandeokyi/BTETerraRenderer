package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.exp.func.JsonParameters;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class DefineFunctionExpression extends JsonExpression {

    private final String name;
    private final JsonParameters parameters;
    private final JsonExpression expression;
    private final ExpressionCallerInfo info;

    @JsonCreator
    @JsonExpressionCreator
    public DefineFunctionExpression(@JsonProperty(value = "name", required = true) String name,
                                    @JsonProperty(value = "parameters", required = true) JsonParameters parameters,
                                    @JsonProperty(value = "expression", required = true) JsonExpression expression) {
        this.name = name;
        this.parameters = parameters;
        this.expression = expression;
        this.info = new ExpressionCallerInfo(this, this.name);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        try {
            JsonScriptValue function = JsonScriptValue.function(this.name,
                    this.parameters, this.expression, runtime.getCurrentScope());
            runtime.getCurrentScope().declareVariable(this.name, function);
            return ExpressionResult.ok(function);
        } catch(Exception e) {
            return ExpressionResult.error(e.getMessage(), this.info);
        }
    }
}
