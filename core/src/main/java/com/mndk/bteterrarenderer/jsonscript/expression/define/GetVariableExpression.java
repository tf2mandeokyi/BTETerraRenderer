package com.mndk.bteterrarenderer.jsonscript.expression.define;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class GetVariableExpression extends JsonExpression {

    private final String name;
    private final ExpressionCallerInfo info;

    @JsonCreator
    public GetVariableExpression(String name) {
        this.name = name;
        this.info = new ExpressionCallerInfo(this, name);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        JsonScriptValue value = runtime.getCurrentScope().getVariableValue(this.name);
        if(value == null) {
            return ExpressionResult.error("variable " + this.name + " not defined", this.info);
        }

        return ExpressionResult.ok(value);
    }
}
