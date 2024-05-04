package com.mndk.bteterrarenderer.jsonscript.expression.define;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpressionCreator;

import javax.annotation.Nonnull;

@JsonDeserialize
public class DeclareVariableExpression extends JsonExpression {

    private final String name;
    private final JsonExpression expression;
    private final ExpressionCallerInfo info;

    @JsonCreator
    @JsonExpressionCreator
    public DeclareVariableExpression(@JsonProperty(value = "name", required = true) String name,
                                     @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.name = name;
        this.expression = expression;
        this.info = new ExpressionCallerInfo(this, this.name);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = this.expression.run(runtime, this.info);
        if(result.isBreakType()) return result;

        runtime.getCurrentScope().declareVariable(this.name, result.getValue());
        return result;
    }
}
