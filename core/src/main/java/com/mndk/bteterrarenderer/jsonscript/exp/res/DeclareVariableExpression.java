package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;

import javax.annotation.Nonnull;

@JsonDeserialize
public class DeclareVariableExpression implements JsonExpression {

    private final String name;
    private final JsonExpression expression;

    @JsonCreator
    @JsonExpressionCreator
    public DeclareVariableExpression(@JsonProperty(value = "name", required = true) String name,
                                     @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        ExpressionResult result = this.expression.run(runtime);
        if(result.isBreakType()) return result;

        runtime.declareVariable(this.name, result.getValue());
        return result;
    }
}
