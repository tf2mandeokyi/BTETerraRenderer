package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ArrayArgumentAcceptable;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.LiteralValueExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonDeserialize
public class AssignOperatorExpression extends JsonExpression {

    private final String name;
    @Nullable
    private final JsonBinaryOperator operator;
    private final JsonExpression expression;
    private final ExpressionCallerInfo info;

    @JsonCreator
    @ArrayArgumentAcceptable
    public AssignOperatorExpression(@JsonProperty(value = "name", required = true) String name,
                                    @JsonProperty(value = "operator", required = true) String symbol,
                                    @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.name = name;
        this.expression = expression;
        this.info = new ExpressionCallerInfo(this, name);

        if(!symbol.startsWith("=")) {
            throw new IllegalArgumentException("assign operators must start with '='");
        }
        symbol = symbol.substring(0, symbol.length() - 1);
        JsonBinaryOperator operator = symbol.isEmpty() ? null : JsonBinaryOperator.valueOf(symbol);

        if(operator != null && !operator.isAssignable()) {
            throw new IllegalArgumentException("operator '" + operator + "' is not assignable");
        }
        this.operator = operator;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = this.expression.run(runtime, this.info);
        if(result.isBreakType()) return result;
        JsonScriptValue assignerValue = result.getValue();

        JsonScriptScope currentScope = runtime.getCurrentScope();
        JsonScriptValue originalValue = currentScope.getVariableValue(this.name);
        if(originalValue == null) {
            return ExpressionResult.error("variable " + this.name + " not defined", this.info);
        }

        if(this.operator != null) {
            JsonExpression originalValueWrapped = new LiteralValueExpression(originalValue);
            JsonExpression assignerValueWrapped = new LiteralValueExpression(assignerValue);

            result = this.operator.run(runtime, originalValueWrapped, assignerValueWrapped);
            if(result.isBreakType()) return result;

            assignerValue = result.getValue();
        }

        currentScope.assignToVariable(name, assignerValue);
        return result;
    }
}
