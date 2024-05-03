package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class AssignToVariableExpression extends JsonExpression {

    private final String name;
    private final JsonExpression expression;
    private final ExpressionCallerInfo info;

    public AssignToVariableExpression(@JsonProperty(value = "name", required = true) String name,
                                      @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.name = name;
        this.expression = expression;
        this.info = new ExpressionCallerInfo(this, this.name);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = expression.run(runtime, this.info);
        if(result.isBreakType()) return result;

        JsonScriptValue originalValue = runtime.getCurrentScope().getVariableValue(this.name);
        if(originalValue == null) {
            return ExpressionResult.error("variable " + this.name + " not defined", this.info);
        }

        runtime.getCurrentScope().assignToVariable(this.name, result.getValue());
        return result;
    }
}
